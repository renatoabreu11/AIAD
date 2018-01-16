package utils;

import java.io.Serializable;

import parkingLot.GlobalVars.WEEKDAY;

public class DailyInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5477026229300722690L;
	private WEEKDAY day;
	private int[] entriesPerHour;
	private int[] exitsPerHour;
	private double totalProfit;
	private int totalDrivers;
	private double profitPerDriver;
	
	/**
	 * DailyInfo constructor. This class keeps all the information about a park during a single day
	 * @param ps
	 */
	public DailyInfo(WEEKDAY day) {
		this.day = day;
		setEntriesPerHour(new int[24]);
		setExitsPerHour(new int[24]);
		totalProfit = 0;
		totalDrivers = 0;
	}
	
	public void addDriver(double finalPrice, int hour) {
		this.totalDrivers++;
		this.totalProfit += finalPrice;
		entriesPerHour[hour]++;
	}
	
	public void removeDriver(int hour) {
		exitsPerHour[hour]++;
	}

	/**
	 * Default getters and setters
	 * @return
	 */
	public double getTotalProfit() {
		return totalProfit;
	}

	public void setTotalProfit(double totalProfit) {
		this.totalProfit = totalProfit;
	}

	public int getTotalDrivers() {
		return totalDrivers;
	}

	public void setTotalDrivers(int totalDrivers) {
		this.totalDrivers = totalDrivers;
	}

	public WEEKDAY getDay() {
		return day;
	}

	public void setDay(WEEKDAY day) {
		this.day = day;
	}

	public int[] getEntriesPerHour() {
		return entriesPerHour;
	}

	public void setEntriesPerHour(int[] entriesPerHour) {
		this.entriesPerHour = entriesPerHour;
	}

	public int[] getExitsPerHour() {
		return exitsPerHour;
	}

	public void setExitsPerHour(int[] exitsPerHour) {
		this.exitsPerHour = exitsPerHour;
	}

	public void endDay() {
		if(totalDrivers > 0)
			profitPerDriver = totalProfit / totalDrivers;
		else profitPerDriver = 0;
	}

	public double getProfitPerDriver() {
		return profitPerDriver;
	}

	public void setProfitPerDriver(double profitPerDriver) {
		this.profitPerDriver = profitPerDriver;
	}
}
