package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.agent.IAgent;
import repastcity3.main.GlobalVars;

public class ParkingContext extends DefaultContext<IAgent>{
	
	public ParkingContext() {
		super(GlobalVars.CONTEXT_NAMES.PARKING_CONTEXT);
	}
	
}
