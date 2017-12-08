package Utils;

import java.util.ArrayList;

public class WeeklyInfo {
	private PricingScheme pricingScheme;
	private ArrayList<DailyInfo> dailyInfo = new ArrayList<>();
	private double totalProfit;
	private double totalDrivers;
	
	/**
	 * WeeklyInfo constructor. This class keeps all the information about a park during a full week
	 * @param ps
	 */
	public WeeklyInfo(PricingScheme ps) {
		this.setPricingScheme(ps);
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
		return dailyInfo;
	}

	public void setDailyInfo(ArrayList<DailyInfo> dailyInfo) {
		this.dailyInfo = dailyInfo;
	}

	public double getTotalDrivers() {
		return totalDrivers;
	}

	public void setTotalDrivers(double totalDrivers) {
		this.totalDrivers = totalDrivers;
	}
}
