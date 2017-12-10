package agents.driver;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import agents.Agent;
import agents.parkingLot.ParkingLot;
import behaviours.RequestEntryPerformer;
import behaviours.RequestExitPerformer;
import environment.Route;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import parkingLot.Initializer;
import parkingLot.Manager;
import parkingLot.Simulation;
import repast.simphony.engine.schedule.ScheduledMethod;
import sajas.core.AID;
import sajas.domain.DFService;

public abstract class Driver extends Agent {
	public static enum DriverState {
		ENTER, // Entering system
		MOVING, // Moving to park
		PICKING, // Picking a park
		REQUEST, // Request to entry
		PARKED, // Parked at the selected park
		EXIT // Exiting system
	}
	
	private static Logger LOGGER = Logger.getLogger(Driver.class.getName());
	
	public static double alfa = 0.5;
	public static double beta = 0.5;

	private int durationOfStay = 10; // definir valor default futuramente
	protected double walkDistance = 600.0; // definir valor default futuramente
	private double defaultSatisfaction = 0.5;
	private double walkCoefficient = 0.5;
	private double payCoefficient = 0.5;

	private DriverState state; 
	private int parkedTime = 0;

	protected ArrayList<ParkComparable> parksInRange = new ArrayList<>();
	private ParkingLot parkingLotDestiny = null;

	public Coordinate destination;
	public Coordinate currentPosition;

	Route route;

	/**
	 * Driver constructor
	 * @param srcPosition
	 * @param destPosition
	 * @param durationOfStay
	 * @param walkDistance
	 */
	public Driver(String name, Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction, Type type) {
		super(name, type);
		this.state = DriverState.ENTER;
		this.durationOfStay = durationOfStay;
		this.walkDistance = walkDistance;
		this.defaultSatisfaction = defaultSatisfaction;
		setPosition(srcPosition,destPosition);
	}

	@Override
	protected void setup() {
		this.logMessage("Driver " + getAID().getName()  + " is ready!");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("Driver");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		this.logMessage("Driver " + getAID().getName()  + " terminating");
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		Agent.updateTick();
		switch(this.state) {
			case ENTER: {
				LOGGER.log(Level.FINE, this.getName() + " is entering the system.");
				break;
			}
			case MOVING: {
				if (!this.route.atDestination()) {
					try {
						this.route.travel();
						this.currentPosition = Simulation.getAgentGeography().getGeometry(this).getCoordinate();
					} catch (Exception e) {
						e.printStackTrace();
					}
					LOGGER.log(Level.FINE,
							this.getName() + " travelling to " + this.route.getDestinationBuilding().toString());
				} else {
					this.state = DriverState.REQUEST;
					addBehaviour(new RequestEntryPerformer((AID) parkingLotDestiny.getAID(), this.getDurationOfStay()));
				}
				break;
			}
			case PICKING: {
				LOGGER.log(Level.FINE, this.getName() + " is picking a new park.");
				if(parksInRange.size() == 0 && parkingLotDestiny == null)
					this.getPossibleParks();
				else this.updatePossibleParks();
				this.pickParkToGo();
				break;
			}
			case REQUEST: {
				LOGGER.log(Level.FINE, this.getName() + " is requesting a entry to the park.");
				break;
			}
			case PARKED: {
				LOGGER.log(Level.FINE, this.getName() + " is parked.");
				if(parkedTime == durationOfStay) {
					addBehaviour(new RequestExitPerformer(this, (AID) this.parkingLotDestiny.getAID()));
				}
				++parkedTime;
				break;
			}
			case EXIT: {
				Simulation.removeAgent(this);
				Initializer.agentManager.removeAgent(this.getAID().toString());
				this.doDelete();
			}
			default:
				break;
		}
	}

	abstract void updatePossibleParks();

	abstract void getPossibleParks();

	public void pickParkToGo() {
		if(this.parksInRange.size() == 0) {
			this.state = DriverState.EXIT;
			Initializer.manager.addUtility(Manager.noParkAvailableUtility);
		}
		else {
			this.state = DriverState.MOVING;
			String aid = this.parksInRange.get(0).park;
			this.parkingLotDestiny = Initializer.agentManager.getAgent(aid);
			this.route = new Route(this, parkingLotDestiny.getPosition(), parkingLotDestiny);
			this.parksInRange.remove(0);
			LOGGER.log(Level.FINE, this.toString() + " created new route to " + parkingLotDestiny.toString());
		}
	}

	/**
	 * Function that calculates the driver utility in a given park
	 * @param price
	 * @return
	 */
	public double getUtility(double price) {
		double[] distAndAng = new double[2];
		Route.distance(this.destination, this.currentPosition, distAndAng);

		double durationOfStayHour = durationOfStay / Manager.ticksPerHour;
		double toPayDuringStay = alfa * price * durationOfStayHour;
		double effortToArriveAtDest = beta * distAndAng[0];
		double utility = defaultSatisfaction - payCoefficient * Math.pow(toPayDuringStay, 0.9) 
				- walkCoefficient * Math.pow(effortToArriveAtDest, 0.9);
		System.out.println("Utility: " + utility);
		/*		
			The powers u and v are both set to 0.9. They create non-linearity in the
			impact of price and effort on the walking distance. Indeed, it is fair to assume
			that a fixed increase in price (e.g. a 1e increase) would have more impact on a
			customer whose original price to pay was low (e.g. original price of 2e) than on
			a customer whose original price to pay was higher (e.g. original price of 200e).
			The concavity of the utility function, ensured by setting u ∈ (0, 1) and v ∈ (0, 1),
			will reflect this concept in the driver’s decision process.
		 */

		return utility;
	}
	
	/**
	 * Updates the driver coordinates
	 * @param initialCoordinate
	 * @param finalCoordinate
	 */
	public void setPosition(Coordinate initialCoordinate, Coordinate finalCoordinate) {
		this.currentPosition = initialCoordinate;
		this.destination = finalCoordinate;
		this.state = DriverState.PICKING;
	}
	
	class ParkComparable{
		public String park;
		public double distance = -1;
		public double utility = -1;
		
		public ParkComparable(String parkAID) {
			this.park = parkAID;
		}
		
		public void setDistance(double distance) {
			this.distance = distance;
		}
		
		public void setUtility(double utility) {
			this.utility = utility;
		}
	}

	/**
	 * Default Getters and Setters
	 * @return
	 */

	public int getDurationOfStay() {
		return durationOfStay;
	}

	public Coordinate getDestination() {
		return destination;
	}

	public Coordinate getCurrentPosition() {
		return currentPosition;
	}
	
	public ParkingLot getParkingLotDestiny() {
		return parkingLotDestiny;
	}

	public void logMessage(String message) {
		LOGGER.fine(message);
	}

	public DriverState getState() {
		return state;
	}

	public void setState(DriverState state) {
		this.state = state;
	}
}
