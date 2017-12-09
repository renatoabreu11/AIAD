package utils;

import java.io.Serializable;
import java.util.ArrayList;

import parkingLot.Initializer;
import parkingLot.GlobalVars.WEEKDAY;

public class WeeklyInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PricingScheme pricingScheme;
	private ArrayList<DailyInfo> days = new ArrayList<>();
	private double totalProfit;
	private int totalDrivers;
	
	/**
	 * WeeklyInfo constructor. This class keeps all the information about a park during a full week
	 * @param ps
	 */
	public WeeklyInfo(PricingScheme ps) {
		this.setPricingScheme(ps);
		days.add(new DailyInfo(WEEKDAY.MONDAY));
		days.add(new DailyInfo(WEEKDAY.TUESDAY));
		days.add(new DailyInfo(WEEKDAY.WEDNESDAY));
		days.add(new DailyInfo(WEEKDAY.THURSDAY));
		days.add(new DailyInfo(WEEKDAY.FRIDAY));
		days.add(new DailyInfo(WEEKDAY.SATURDAY));
		days.add(new DailyInfo(WEEKDAY.SUNDAY));
		totalProfit = 0;
		totalDrivers = 0;
	}
	
	public WeeklyInfo() {
		pricingScheme = new PricingScheme();
		days.add(new DailyInfo(WEEKDAY.MONDAY));
		days.add(new DailyInfo(WEEKDAY.TUESDAY));
		days.add(new DailyInfo(WEEKDAY.WEDNESDAY));
		days.add(new DailyInfo(WEEKDAY.THURSDAY));
		days.add(new DailyInfo(WEEKDAY.FRIDAY));
		days.add(new DailyInfo(WEEKDAY.SATURDAY));
		days.add(new DailyInfo(WEEKDAY.SUNDAY));
		totalProfit = 0;
		totalDrivers = 0;
	}

	public void addDriver(double finalPrice) {
		totalProfit += finalPrice;
		totalDrivers++;
		DailyInfo di = days.get(Initializer.manager.getDay());
		di.addDriver(finalPrice, Initializer.manager.getHour());
	}

	public void removeDriver() {
		DailyInfo di = days.get(Initializer.manager.getDay());
		di.removeDriver(Initializer.manager.getHour());
	}

	/**
	 * Default getters and setters
	 * @return
	 */
	
	public PricingScheme getPricingScheme() {
		return pricingScheme;
	}

	public void setPricingScheme(PricingScheme pricingScheme) {
		this.pricingScheme = pricingScheme;
	}

	public double getTotalProfit() {
		return totalProfit;
	}

	public void setTotalProfit(double totalProfit) {
		this.totalProfit = totalProfit;
	}

	public ArrayList<DailyInfo> getDailyInfo() {
		return days;
	}

	public void setDailyInfo(ArrayList<DailyInfo> dailyInfo) {
		this.days = dailyInfo;
	}

	public int getTotalDrivers() {
		return totalDrivers;
	}

	public void setTotalDrivers(int totalDrivers) {
		this.totalDrivers = totalDrivers;
	}

	public double calculatePrice(double durationOfStay, double scale) {
		return pricingScheme.calculatePrice(durationOfStay, scale);
	}
}
