package agents.driver;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import agents.Agent;
import agents.parkingLot.ParkingLot;
import behaviours.RequestEntryPerformer;
import environment.Route;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import parkingLot.Initializer;
import parkingLot.Simulation;
import agents.AgentManager;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.IndexedIterable;
import sajas.core.AID;
import sajas.domain.DFService;

public abstract class Driver extends Agent {
	private static Logger LOGGER = Logger.getLogger(Driver.class.getName());
	
	public static double alfa = 0.5;
	public static double beta = 0.5;

	private int durationOfStay = 10; // definir valor default futuramente
	private double walkDistance = 400.0; // definir valor default futuramente
	private double defaultSatisfaction = 0.5;
	private double walkCoefficient = 0.5;
	private double payCoefficient = 0.5;

	private boolean alive = true;
	private boolean inPark = false;

	private ArrayList<ParkingLot> parksInRange = new ArrayList<>();
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
		if(this.alive) {
			Agent.updateTick();
			if (!this.route.atDestination()) {
				try {
					this.route.travel();
				} catch (Exception e) {
					e.printStackTrace();
				}
				LOGGER.log(Level.FINE,
						this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
			} else {
				if(!this.inPark) {
					this.inPark = true;
					
					addBehaviour(new RequestEntryPerformer((AID) parkingLotDestiny.getAID(), this.getDurationOfStay()));
				}
				LOGGER.log(Level.FINE, this.toString() + " reached final destination: " + this.route.getDestinationBuilding().toString());
			}
		} else {
			Simulation.removeAgent(this);
			Initializer.agentManager.removeAgent(this.getAID().toString());
			this.doDelete();
		}
	}

	private void getPossibleParks() {
		Coordinate tmp = new Coordinate();
		double[] distAndAng = new double[2];
		IndexedIterable<ParkingLot> parks = Simulation.parkingLotContext.getObjects(ParkingLot.class);

		for(int i = 0;i<parks.size();i++) {
			tmp = parks.get(i).getPosition();
			Route.distance(this.destination, tmp, distAndAng);
			System.out.println("I"+i+": "+distAndAng[0]+" ; "+distAndAng[1]);
			if(distAndAng[0] < this.walkDistance) {
				parksInRange.add(parks.get(i));
			}
		}
	}

	public void pickParkToGo() {
		if(this.parksInRange.size() == 0) {
			this.alive = false;
		}
		else {
			this.parkingLotDestiny = this.parksInRange.get(0);
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
		
		this.getPossibleParks();
		this.pickParkToGo();
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

	public boolean getAlive() {
		return alive;
	}

	public void logMessage(String message) {
		LOGGER.info(message);
	}

	public void setAlive(boolean b) {
		this.alive = b;
	}
}
