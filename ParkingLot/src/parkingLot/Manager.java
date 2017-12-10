package parkingLot;

import java.util.logging.Logger;

import agents.Agent;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Manager extends Agent {
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	public static int ticksPerWeek = 24000*7;
	public static int ticksPerDay = 24000;
	public static int ticksPerHour = 1000;
	public static double noParkAvailableUtility = -50.0; // TODO change this 
	
	private int totalTicks;
	
	private int week;
	private GlobalVars.WEEKDAY day;
	private int hour;
	private int currentTickInWeek;
	private int currentTickInDay;
	private int currentTickInHour;
	
	private double globalUtility;
	
	public Manager() {
		super("Manager", Type.MANAGER);
		setTotalTicks(0);
		
		setWeek(0);
		day = GlobalVars.WEEKDAY.MONDAY;
		setHour(0);
		setCurrentTickInDay(0);
		setCurrentTickInHour(0);
		setCurrentTickInWeek(0);
		
		globalUtility = 0;
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
	
	public int getPreviousDay() {
		return (this.day.id - 1 + 7) % 7;
	}

	public int getCurrentTickInWeek() {
		return currentTickInWeek;
	}

	public void setCurrentTickInWeek(int currentTickInWeek) {
		this.currentTickInWeek = currentTickInWeek;
	}

	public double getGlobalUtility() {
		return globalUtility;
	}

	public void setGlobalUtility(double globalUtility) {
		this.globalUtility = globalUtility;
	}

	public void addUtility(double driverUtility) {
		this.globalUtility += driverUtility;
	}
	
	public int calculateNumberOfDrivers() {
		double currentHour = Math.floorDiv(currentTickInDay, 1000);
		
		int numDrivers=(int)((9.7465137943127814*10)+(4.1313445183117764*10*currentHour)+(-5.0378347149419739*10*Math.pow(currentHour, 2))+
				(1.9382369234511167*10*Math.pow(currentHour, 3))+(-3.3184195775022003*Math.pow(currentHour, 4))+
				(2.9929797307649647/10*Math.pow(currentHour, 5))+(-1.4828693808010440/100*Math.pow(currentHour, 6))+
				(3.8165393557919086/10000*Math.pow(currentHour, 7))+(-3.9870796373014557/1000000*Math.pow(currentHour,8)));
		
		System.out.println("Current hour: "+currentHour+", number drivers: "+numDrivers+" "+currentTickInDay);
		
		return numDrivers;
	}
}
