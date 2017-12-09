package agents.driver;

import java.util.ArrayList;
import java.util.Comparator;
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
import repast.simphony.util.collections.IndexedIterable;
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
	private double walkDistance = 600.0; // definir valor default futuramente
	private double defaultSatisfaction = 0.5;
	private double walkCoefficient = 0.5;
	private double payCoefficient = 0.5;

	private DriverState state; 
	private int parkedTime = 0;

	private ArrayList<ParkDistance> parksInRange = new ArrayList<>();
	private int currentParkSelected = -1;
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
	public Driver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super("Driver", Type.RATIONAL_DRIVER);
		this.state = DriverState.ENTER;
		this.destination = destPosition;
		this.currentPosition = srcPosition;
		this.durationOfStay = durationOfStay;
		this.walkDistance = walkDistance;
		this.defaultSatisfaction = defaultSatisfaction;
	}
	
	/**
	 * Default constructor
	 * @param name
	 * @param type
	 */
	public Driver(String name, Type type) {
		super(name, type);
		this.state = DriverState.ENTER;
	}

	@Override
	protected void setup() {
		LOGGER.info("Driver " + getAID().getName()  + " is ready!");
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

		LOGGER.info("Driver " + getAID().getName()  + " terminating");
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

	private void getPossibleParks() {
		Coordinate tmp = new Coordinate();
		double[] distAndAng = new double[2];
		IndexedIterable<ParkingLot> parks = Simulation.parkingLotContext.getObjects(ParkingLot.class);

		for(int i = 0;i<parks.size();i++) {
			tmp = parks.get(i).getPosition();
			Route.distance(this.destination, tmp, distAndAng);
			System.out.println("I"+i+": "+distAndAng[0]);
			if(distAndAng[0] < this.walkDistance) {
				parksInRange.add(new ParkDistance(parks.get(i),distAndAng[0]));
			}
		}
		
		System.out.println("Sem ordem");
		for(int i=0;i<parksInRange.size();i++) {
			System.out.println(parksInRange.get(i));
		}
		
		if(this.type == Type.RATIONAL_DRIVER) {
			System.out.println("Racional");
			parksInRange.sort(new Comparator<ParkDistance>() {
		        @Override
		        public int compare(ParkDistance pd1, ParkDistance pd2) {
		            return pd1.compareTo(pd2);
		        }
		    });
		}
		
		System.out.println("Com ordem");
		for(int i=0;i<parksInRange.size();i++) {
			System.out.println(parksInRange.get(i));
		}
	}

	public void pickParkToGo() {
		this.currentParkSelected++;
		System.out.println("Vou escolher o park indice: "+this.currentParkSelected);
		if(this.currentParkSelected >= this.parksInRange.size()) {
			this.state = DriverState.EXIT;
			Initializer.manager.addUtility(Manager.noParkAvailableUtility);
		}
		else {
			this.state = DriverState.MOVING;
			this.parkingLotDestiny = this.parksInRange.get(this.currentParkSelected).park;
			this.route = new Route(this, Simulation.getAgentGeography().getGeometry(parkingLotDestiny).getCoordinate(), parkingLotDestiny);
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

		double toPayDuringStay = alfa * price * durationOfStay;
		double effortToArriveAtDest = beta * distAndAng[0];
		double utility = defaultSatisfaction - payCoefficient * Math.pow(toPayDuringStay, 0.9) 
				- walkCoefficient * Math.pow(effortToArriveAtDest, 0.9);

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
	public void setPositions(Coordinate initialCoordinate, Coordinate finalCoordinate) {
		this.currentPosition = initialCoordinate;
		this.destination = finalCoordinate;
		
		this.state = DriverState.PICKING;
		this.getPossibleParks();
		this.pickParkToGo();
	}
	
	private class ParkDistance implements Comparable<ParkDistance>{
		public ParkingLot park;
		public double distance;
		
		public ParkDistance(ParkingLot park, double distance) {
			this.park = park;
			this.distance = distance;
		}

		@Override
		public int compareTo(ParkDistance arg) {
			return (int)(this.distance-arg.distance);
		}
		
		public String toString() {
			return "Park: "+park.getName()+"; "+distance;
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
		LOGGER.info(message);
	}

	public DriverState getState() {
		return state;
	}

	public void setState(DriverState state) {
		this.state = state;
	}
}
