package parkingLot;

import java.util.Iterator;
import java.util.logging.Logger;

import agents.Driver;
import agents.Agent;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Manager extends Agent{
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	
	private GlobalVars.WEEKDAY weekday;
	private int currentTicksInDay;
	
	public Manager() {
		super("Manager", Type.MANAGER);
		weekday = GlobalVars.WEEKDAY.SUNDAY;
		currentTicksInDay = 0;
	}
	
	@ScheduledMethod(start = 1, interval = 12000)
	public void update() {
		currentTicksInDay = 0;
		weekday = GlobalVars.WEEKDAY.getNextDay(weekday.id);
		LOGGER.severe("Started new day: " + weekday.toString());
	/*	Iterator<Agent> iterator = Initializer.agentContext.getRandomObjects(Driver.class, 1).iterator();
		if(!iterator.hasNext()) return;
		Agent driver = iterator.next();
		Simulation.removeAgent(driver);*/
	}
	
	public void setup() {
		
	}
	
	public void finalize() {
		
	}
}
