package agents;

import java.util.HashMap;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Coordinate;

import sajas.domain.*;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class ParkingLot extends Agent {
	private static Logger LOGGER = Logger.getLogger(ParkingLot.class.getName());
	
	public int capacity;
	protected int currLotation = 0;
	protected HashMap<String, Driver> parkedDrivers = new HashMap<String, Driver>();
	
	public double pricePerMinute;
	public double minPricePerStay;
	public double maxPricePerStay;
	
	public double profit = 0;
	private Coordinate position;
	
	/**
	 * Parking facility constructor
	 * @param type
	 * @param id
	 * @param position
	 * @param maxCapacity
	 */
	public ParkingLot(Type type, String id, Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super("park");
		this.type = type;
		this.id = id;
		this.position = position;
		this.capacity = maxCapacity;
	}
	
	public void update() {};
	
	public ParkingLot(Coordinate position) { //construtor tempor�rio
		super("park");
		this.position = position;
		this.capacity = 10;
	}
	
	@Override
	protected void setup() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("ParkingLot");
		sd.setType("ParkingLot");
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

		LOGGER.info("Parking facility terminating");
	}

	
	/**
	 * Returns the price to pay for the stay
	 * @param driver
	 * @return
	 */
	public double getFinalPrice(Driver driver) {
		double price = pricePerMinute * 1.0; // driver.getStaytime
		
		if(price > maxPricePerStay) {
			price = maxPricePerStay;
		} else if (price < minPricePerStay) {
			price = minPricePerStay;
		}
		
		double scale = currLotation / capacity;
		
		if(scale <= 0.3) {
			
		} else if (scale >= 0.7) {
			
		}
		
		return price;
	}
	
	/**
	 * Removes a driver from the park
	 * @param driver
	 */
	public void removeDriver(Driver driver) {
		parkedDrivers.remove(driver.id);
		currLotation--;
		System.out.println("Park: "+this.id+" ; Driver: "+driver.id+" ; "+this.currLotation);
	}
	
	/**
	 * Accepts or reject a new driver, accordingly to the current capacity
	 * @param driver
	 * @return
	 */
	public boolean acceptDriver(Driver driver) {
		if(currLotation == capacity) {
			return false;
		}
		
		double finalPrice = getFinalPrice(driver);
		profit += finalPrice;
		parkedDrivers.put(driver.id, driver);
		currLotation++;
		System.out.println("Park: "+this.id+" ; Driver: "+driver.id+" ; "+this.currLotation);
		return true;
	}
	
	/**
	 * Removes all drivers from the park
	 */
	public void closeParkingFacility() {
		parkedDrivers = new HashMap<String, Driver>();
		currLotation = 0;
	}
	
	/**
	 * Default Getters and Setters
	 */
	
	public void setPricingScheme(double pricePerMinute, double minPricePerStay, double maxPricePerStay) {
		this.pricePerMinute = pricePerMinute;
		this.minPricePerStay = minPricePerStay;
		this.maxPricePerStay = maxPricePerStay;
	}
	
	public Coordinate getPosition() {
		return position;
	}

	public void setPosition(Coordinate position) {
		this.position = position;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int getCurrLotation() {
		return currLotation;
	}

	public void setCurrLotation(int currLotation) {
		this.currLotation = currLotation;
	}

	public HashMap<String, Driver> getParkedDrivers() {
		return parkedDrivers;
	}

	public void setParkedDrivers(HashMap<String, Driver> parkedDrivers) {
		this.parkedDrivers = parkedDrivers;
	}

	public double getPricePerMinute() {
		return pricePerMinute;
	}

	public void setPricePerMinute(double pricePerMinute) {
		this.pricePerMinute = pricePerMinute;
	}

	public double getMinPricePerStay() {
		return minPricePerStay;
	}

	public void setMinPricePerStay(double minPricePerStay) {
		this.minPricePerStay = minPricePerStay;
	}

	public double getMaxPricePerStay() {
		return maxPricePerStay;
	}

	public void setMaxPricePerStay(double maxPricePerStay) {
		this.maxPricePerStay = maxPricePerStay;
	}
	
	public String getParkingFacilityInfo() {
		return "";
	}
}
