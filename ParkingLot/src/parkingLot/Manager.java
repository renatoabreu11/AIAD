package parkingLot;

import java.util.logging.Logger;

import agents.IAgent;

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
			weekday = GlobalVars.WEEKDAY.getNextDay(weekday.id);
			LOGGER.info("Started new day: " + weekday.toString());
		}
	}
}
