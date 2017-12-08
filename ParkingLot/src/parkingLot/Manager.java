package parkingLot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Point;

import agents.Driver;
import agents.IAgent;
import environment.Junction;
import environment.Road;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Manager extends IAgent{
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	
	private GlobalVars.WEEKDAY weekday;
	private int currentTicksInDay;
	
	public Manager() {
		weekday = GlobalVars.WEEKDAY.SUNDAY;
		currentTicksInDay = 0;
	}
	
	@ScheduledMethod(start = 1, interval = 12000)
	public void update() {
		currentTicksInDay = 0;
		weekday = GlobalVars.WEEKDAY.getNextDay(weekday.id);
		LOGGER.severe("Started new day: " + weekday.toString());
		Iterator<IAgent> iterator = Initializer.agentContext.getRandomObjects(Driver.class, 1).iterator();
		if(!iterator.hasNext()) return;
		IAgent driver = iterator.next();
		Initializer.removeAgent(driver);
	}
	
	public void setup() {
		
	}
	
	public void finalize() {
		
	}
}
