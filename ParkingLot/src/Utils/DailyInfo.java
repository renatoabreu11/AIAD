package Utils;

import java.util.ArrayList;

public class DailyInfo {
	
	private ArrayList<Integer> entriesPerHour = new ArrayList<>();
	private ArrayList<Integer> exitsPerHour = new ArrayList<>();
	private double totalProfit;
	private double totalDrivers;
	
	/**
	 * DailyInfo constructor. This class keeps all the information about a park during a single day
	 * @param ps
	 */
	public DailyInfo() {
		
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

	public ArrayList<Integer> getExitsPerHour() {
		return exitsPerHour;
	}

	public void setExitsPerHour(ArrayList<Integer> exitsPerHour) {
		this.exitsPerHour = exitsPerHour;
	}

	public ArrayList<Integer> getEntriesPerHour() {
		return entriesPerHour;
	}

	public void setEntriesPerHour(ArrayList<Integer> entriesPerHour) {
		this.entriesPerHour = entriesPerHour;
	}
}
