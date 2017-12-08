package parkingLot;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Point;

import agents.Driver;
import agents.IAgent;
import environment.Junction;
import environment.Road;

public class Manager extends IAgent{
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	
	private GlobalVars.WEEKDAY weekday;
	private int currentTicksInDay;
	
	public Manager() {
		weekday = GlobalVars.WEEKDAY.SUNDAY;
		currentTicksInDay = 0;
	}
	
	public void update() {
		if((++currentTicksInDay) >= GlobalVars.WEEKDAY.maxTicksInDay) {
			currentTicksInDay = 0;
			weekday = GlobalVars.WEEKDAY.getNextDay(weekday.id);
			LOGGER.info("Started new day: " + weekday.toString());
			Driver driver = new Driver();
			Road road = Initializer.roadContext.getRandomObject();
			ArrayList<Junction> endpoints = road.getJunctions();
			Point point = Initializer.junctionGeography.getGeometry(endpoints.get(0)).getCentroid();
			Initializer.agentContext.add(driver);
			Initializer.moveAgent(driver, point);
		}
	}
	
	public void setup() {
		
	}
	
	public void finalize() {
		
	}
}
