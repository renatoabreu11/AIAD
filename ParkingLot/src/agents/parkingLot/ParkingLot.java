package agents.parkingLot;

import java.util.HashMap;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Coordinate;

import Utils.PricingScheme;
import Utils.WeeklyInfo;
import agents.Agent;
import behaviours.AcceptEntryServer;
import behaviours.RequestEntryServer;
import behaviours.RequestExitServer;
import behaviours.ShareWeeklyInfoServer;
import behaviours.WeekyUpdatePerformer;
import sajas.domain.*;
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
	
	private HashMap<String, WeeklyInfo> weeklyInfo = new HashMap<String, WeeklyInfo>();
	private PricingScheme pricingScheme;
	private double profit = 0;
	
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
		pricingScheme = new PricingScheme();
	}
	
	public ParkingLot(String name, Type type) {
		super(name, type);
		pricingScheme = new PricingScheme();
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
		// change the second value to the number of ms per week
		addBehaviour(new WeekyUpdatePerformer(this, 10000));
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
		return pricingScheme.calculatePrice(durationOfStay, scale);
	}
	
	/**
	 * Removes a driver from the park
	 * @param string
	 */
	public void removeDriver(String AID) {
		parkedDrivers.remove(AID);
		currLotation--;
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
		this.setProfit(this.getProfit() + finalPrice);
		
		parkedDrivers.put(AID, Integer.parseInt(durationOfStay));
		currLotation++;
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
	 * Saves the info related to the last week
	 */
	public void saveWeeklyInfo() {
		// TODO Auto-generated method stub
		
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

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public HashMap<String, WeeklyInfo> getWeeklyInfo() {
		return weeklyInfo;
	}

	public void setWeeklyInfo(HashMap<String, WeeklyInfo> weeklyInfo) {
		this.weeklyInfo = weeklyInfo;
	}
}
