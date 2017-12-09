package utils;

import java.io.Serializable;

public class PricingScheme implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 403309766518328856L;
	public double pricePerMinute;
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
		double price = pricePerMinute * durationOfStay;
		
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
}
