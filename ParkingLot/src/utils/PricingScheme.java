package utils;

import java.io.Serializable;

import parkingLot.Initializer;

public class PricingScheme implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 403309766518328856L;
	public double pricePerMinute;
	public double[] pricesPerHour; // throughout a day. Each minute will have different values accordingly to the hour
	public double minPricePerStay;
	public double maxPricePerStay;
	
	public PricingScheme(double pricePerMinute, double minPricePerStay, double maxPricePerStay) {
		this.pricePerMinute = pricePerMinute;
		this.minPricePerStay = minPricePerStay;
		this.maxPricePerStay = maxPricePerStay;
	}
	
	public PricingScheme() {
		pricePerMinute = 0.2;
		minPricePerStay = 5;
		maxPricePerStay = 50;
	}
	
	public PricingScheme(double[] pricesPerHour) {
		this.pricePerMinute = -1;
		this.pricesPerHour = pricesPerHour;
		minPricePerStay = 5;
		maxPricePerStay = 50;
	}
	
	public double getPricePerMinute() {
		return pricePerMinute;
	}

	public double getMinPricePerStay() {
		return minPricePerStay;
	}

	public double getMaxPricePerStay() {
		return maxPricePerStay;
	}

	public double calculatePrice(double durationOfStay, double scale) {
		double price;
		if(pricePerMinute == -1) { // depends on the current hour
			price = pricesPerHour[Initializer.manager.getHour()] * durationOfStay;
		} else { // constant price per minute
			price = pricePerMinute * durationOfStay;
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
