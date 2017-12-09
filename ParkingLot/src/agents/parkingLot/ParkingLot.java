package agents.parkingLot;

import java.util.HashMap;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Coordinate;

import agents.Agent;
import behaviours.AcceptEntryServer;
import behaviours.RequestEntryServer;
import behaviours.RequestExitServer;
import behaviours.ShareWeeklyInfoServer;
import sajas.domain.*;
import utils.PricingScheme;
import utils.WeeklyInfo;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import repast.simphony.engine.schedule.ScheduledMethod;

public abstract class ParkingLot extends Agent {
	private static Logger LOGGER = Logger.getLogger(ParkingLot.class.getName());

	// Parking spots info
	protected HashMap<String, Integer> parkedDrivers = new HashMap<String, Integer>();
	public int capacity = 100;
	protected int currLotation = 0;
	
	private WeeklyInfo weeklyInfo;
	private double globalProfit = 0;
	
	private Coordinate position;
	
	/**
	 * Parking facility constructor
	 * @param type
	 * @param id
	 * @param position
	 * @param maxCapacity
	 */
	public ParkingLot(Coordinate position, int maxCapacity, Coordinate currentPosition) {
		super("park");
		this.position = position;
		this.capacity = maxCapacity;
		setWeeklyInfo(new WeeklyInfo());
	}
	
	public ParkingLot(String name, Type type) {
		super(name, type);
		setWeeklyInfo(new WeeklyInfo());
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void update() {};

	@Override
	protected void setup() {
		LOGGER.info("ParkingLot " + getAID().getName()  + " is ready!");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType(this.getType().toString());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println(e.getMessage());
		}
		
		addBehaviour(new AcceptEntryServer());
		addBehaviour(new RequestEntryServer());
		addBehaviour(new RequestExitServer());
		if(this.getType().equals(Type.COOPERATIVE_PARKING_LOT))
			addBehaviour(new ShareWeeklyInfoServer());
	}
	
	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		LOGGER.info("Parking lot terminating");
	}
	
	/**
	 * Returns the price to pay for the stay
	 * @param durationOfStay
	 * @return
	 */
	public double getFinalPrice(String durationOfStayStr) {
		double durationOfStay = Double.parseDouble(durationOfStayStr);
		double scale = currLotation / capacity;
		return weeklyInfo.calculatePrice(durationOfStay, scale);
	}
	
	/**
	 * Removes a driver from the park
	 * @param string
	 */
	public void removeDriver(String AID) {
		parkedDrivers.remove(AID);
		currLotation--;
		weeklyInfo.removeDriver();
	}

	/**
	 * Accepts or reject a new driver, accordingly to the current capacity
	 * @param driver
	 * @return
	 */
	public boolean acceptDriver(String durationOfStay, String AID) {
		if(currLotation == capacity) {
			return false;
		}
		double finalPrice = this.getFinalPrice(durationOfStay);
		this.setGlobalProfit(this.getGlobalProfit() + finalPrice);
		
		parkedDrivers.put(AID, Integer.parseInt(durationOfStay));
		currLotation++;
		
		weeklyInfo.addDriver(finalPrice);
		
		return true;
	}
	
	/**
	 * Removes all drivers from the park
	 */
	public void closeParkingFacility() {
		parkedDrivers = new HashMap<String, Integer>();
		currLotation = 0;
	}
	
	/**
	 * Default Getters and Setters
	 */
	public Coordinate getPosition() {
		return position;
	}

	public int getCapacity() {
		return capacity;
	}
	
	public int getCurrLotation() {
		return currLotation;
	}

	public HashMap<String, Integer> getParkedDrivers() {
		return parkedDrivers;
	}

	public void setPosition(Coordinate position) {
		this.position = position;
	}

	public void logMessage(String message) {
		LOGGER.info(message);
	}

	public WeeklyInfo getWeeklyInfo() {
		return weeklyInfo;
	}

	public void setWeeklyInfo(WeeklyInfo weeklyInfo) {
		this.weeklyInfo = weeklyInfo;
	}
	
	public double getWeeklyProfit() {
		return weeklyInfo.getTotalProfit();
	}
	
	public double getWeeklyPriceAverage() {
		return 0;
	}

	public double getGlobalProfit() {
		return globalProfit;
	}

	public void setGlobalProfit(double globalProfit) {
		this.globalProfit = globalProfit;
	}
}
