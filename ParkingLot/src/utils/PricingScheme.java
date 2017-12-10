package utils;

import java.io.Serializable;

import parkingLot.Initializer;
import parkingLot.Manager;

public class PricingScheme implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 403309766518328856L;
	public double pricePerHour;
	public double[] pricesPerHour; // throughout a day. Each minute will have different values accordingly to the hour
	public double minPricePerStay;
	public double maxPricePerStay;
	
	public PricingScheme(double pricePerHour, double minPricePerStay, double maxPricePerStay) {
		this.pricePerHour = pricePerHour;
		this.minPricePerStay = minPricePerStay;
		this.maxPricePerStay = maxPricePerStay;
	}
	
	public PricingScheme() {
		pricePerHour = 1;
		minPricePerStay = 5;
		maxPricePerStay = 50;
	}
	
	public PricingScheme(double[] pricesPerHour, double minPrice, double maxPrice) {
		this.pricePerHour = -1;
		this.pricesPerHour = pricesPerHour;
		minPricePerStay = minPrice;
		maxPricePerStay = maxPrice;
	}
	
	public PricingScheme(double[] pricesPerHour) {
		this.pricePerHour = -1;
		this.pricesPerHour = pricesPerHour;
		minPricePerStay = 5;
		maxPricePerStay = 50;
	}

	public double getMinPricePerStay() {
		return minPricePerStay;
	}

	public double getMaxPricePerStay() {
		return maxPricePerStay;
	}
	
	public double getPricePerHour() {
		return pricePerHour;
	}

	public void setPricePerHour(double pricePerHour) {
		this.pricePerHour = pricePerHour;
	}

	public double calculatePrice(double durationOfStay, double scale) {
		double durationOfStayHour = durationOfStay / Manager.ticksPerHour;
		double price;
		if(pricePerHour == -1) { // depends on the current hour
			price = pricesPerHour[Initializer.manager.getHour()] * durationOfStayHour;
		} else { // constant price per minute
			price = pricePerHour * durationOfStayHour;
		}
		
		if(price > maxPricePerStay) {
			price = maxPricePerStay;
		} else if (price < minPricePerStay) {
			price = minPricePerStay;
		}
		
		if(scale < 0.3) {
			price -= price * (0.3 - scale);
		} else if (scale > 0.7) {
			price += price * (scale - 0.7);
		}
		
		return price;
	}
	
	public double[] getPricesPerHour() {
		return pricesPerHour;
	}

	public void setPricesPerHour(double[] pricesPerHour) {
		this.pricesPerHour = pricesPerHour;
	}

}
