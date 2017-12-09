package parkingLot;

import java.util.logging.Logger;

import agents.Agent;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Manager extends Agent {
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	public static int ticksPerWeek = 21000;
	public static int ticksPerDay = 3000;
	public static int ticksPerHour = 125;
	
	private int totalTicks;
	
	private int week;
	private GlobalVars.WEEKDAY day;
	private int hour;
	private int currentTickInWeek;
	private int currentTickInDay;
	private int currentTickInHour;
	
	public Manager() {
		super("Manager", Type.MANAGER);
		setTotalTicks(0);
		
		setWeek(0);
		day = GlobalVars.WEEKDAY.MONDAY;
		setHour(0);
		setCurrentTickInDay(0);
		setCurrentTickInHour(0);
		setCurrentTickInWeek(0);
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		++totalTicks;
		++currentTickInWeek;
		++currentTickInDay;
		++currentTickInHour;
		
		if(currentTickInHour == ticksPerHour) { // next hour
			currentTickInHour = 0;
			hour++;
			if(hour == 24) { // next day
				hour = 0;
				currentTickInDay = 0;
				day = GlobalVars.WEEKDAY.getNextDay(day.id);
				if(day.equals(GlobalVars.WEEKDAY.MONDAY)) { // next week
					week++;
					setCurrentTickInWeek(0);
				}
			}
		}
	}
	
	/**
	 * Default getters and setters
	 * @return
	 */

	public int getTotalTicks() {
		return totalTicks;
	}

	public void setTotalTicks(int totalTicks) {
		this.totalTicks = totalTicks;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getCurrentTickInDay() {
		return currentTickInDay;
	}

	public void setCurrentTickInDay(int currentTickInDay) {
		this.currentTickInDay = currentTickInDay;
	}

	public int getCurrentTickInHour() {
		return currentTickInHour;
	}

	public void setCurrentTickInHour(int currentTickInHour) {
		this.currentTickInHour = currentTickInHour;
	}

	public int getDay() {
		return this.day.id;
	}

	public int getCurrentTickInWeek() {
		return currentTickInWeek;
	}

	public void setCurrentTickInWeek(int currentTickInWeek) {
		this.currentTickInWeek = currentTickInWeek;
	}
}
