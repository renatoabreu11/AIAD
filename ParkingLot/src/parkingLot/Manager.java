package parkingLot;

import java.util.logging.Logger;

import agents.Agent;
import agents.parkingLot.ParkingLot;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.util.collections.IndexedIterable;

public class Manager extends Agent {
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	public static int ticksPerDay = 750;
	public static int ticksPerHour = ticksPerDay/24;
	public static int ticksPerWeek = ticksPerDay*7;

	public static double noParkAvailableUtility = -50.0; // TODO change this 
	
	private int totalTicks;
	
	private int week;
	private GlobalVars.WEEKDAY day;
	private int hour;
	private int currentTickInWeek;
	private double currentTickInDay;
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
					IndexedIterable<ParkingLot> pl = Simulation.parkingLotContext.getObjects(ParkingLot.class);
					for (int i = 0; i < pl.size(); i++) {
						pl.get(i).updatePricingScheme();
					}
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

	public double getCurrentTickInDay() {
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
	
	public int calculateNumberOfDriversWeekDays() {
		double currentHour = currentTickInDay/1000.0;
		int numDrivers= (int) (9.7465137943127814e+001 * Math.pow(currentHour,0)
        +  4.1313445183117764e+001 * Math.pow(currentHour,1)
        + -5.0378347149419739e+001 * Math.pow(currentHour,2)
        +  1.9382369234511167e+001 * Math.pow(currentHour,3)
        + -3.3184195775022003e+000 * Math.pow(currentHour,4)
        +  2.9929797307649647e-001 * Math.pow(currentHour,5)
        + -1.4828693808010440e-002 * Math.pow(currentHour,6)
        +  3.8165393557919086e-004 * Math.pow(currentHour,7)
        + -3.9870796373014557e-006 * Math.pow(currentHour,8));
		
		System.out.println("Current hour: "+currentHour+" "+currentTickInDay);
		
		return numDrivers;
	}
	
	public int calculateNumberOfDriversWeekEndDays() {
		double currentHour = currentTickInDay/1000;
		int numDrivers =  (int) (5.5410955131276346e+001 * Math.pow(currentHour,0)
        +  2.1102033222065248e+001 * Math.pow(currentHour,1)
        + -1.8796855577791021e+001 * Math.pow(currentHour,2)
        +  7.3522350964157379e+000 * Math.pow(currentHour,3)
        + -1.2077376021269184e+000 * Math.pow(currentHour,4)
        +  1.0233526625755751e-001 * Math.pow(currentHour,5)
        + -4.7429521356996658e-003 * Math.pow(currentHour,6)
        +  1.1445930510905407e-004 * Math.pow(currentHour,7)
        + -1.1274232393164334e-006 * Math.pow(currentHour,8));
		
		System.out.println("Current hour: "+currentHour+" "+currentTickInDay);
		
		return numDrivers;
	}
}
