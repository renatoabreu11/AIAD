package Utils;

import parkingLot.GlobalVars.WEEKDAY;

public class DailyInfo {
	private WEEKDAY day;
	private int[] entriesPerHour;
	private int[] exitsPerHour;
	private double totalProfit;
	private double totalDrivers;
	
	/**
	 * DailyInfo constructor. This class keeps all the information about a park during a single day
	 * @param ps
	 */
	public DailyInfo() {
		setEntriesPerHour(new int[24]);
		setExitsPerHour(new int[24]);
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

	public double getTotalDrivers() {
		return totalDrivers;
	}

	public void setTotalDrivers(double totalDrivers) {
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
}
