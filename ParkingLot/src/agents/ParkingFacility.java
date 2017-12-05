package agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import com.vividsolutions.jts.geom.Coordinate;

public class ParkingFacility extends IAgent {
	private static Logger LOGGER = Logger.getLogger(ParkingFacility.class.getName());
	
	public Coordinate position;
	
	public int capacity;
	protected int currLotation;
	// Hashmap with string as driver identifier and driver object
	protected HashMap<String, Driver> parkedDrivers;
	
	// Pricing scheme
	public double pricePerMinute;
	public double minPricePerStay;
	public double maxPricePerStay;
	
	public double profit;
	
	public ParkingFacility(Type type, String id, Coordinate position, int maxCapacity) {
		super();
		this.type = type;
		this.id = id;
		this.position = position;
		this.capacity = maxCapacity;
		this.currLotation = 0;
		this.profit = 0;
		parkedDrivers = new HashMap<String, Driver>();
	}
	
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
	
	public boolean acceptDriver(Driver driver) {
		if(currLotation == capacity) {
			return false;
		}
		
		double finalPrice = getFinalPrice(driver);
		profit += finalPrice;
		parkedDrivers.put(driver.id, driver);
		currLotation++;
		return true;
	}
	
	public void closeParkingFacility() {
		parkedDrivers = new HashMap<String, Driver>();
		currLotation = 0;
	}
	
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
}
