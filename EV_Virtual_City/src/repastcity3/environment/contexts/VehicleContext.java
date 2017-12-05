package repastcity3.environment.contexts;

import repast.simphony.context.DefaultContext;
import repastcity3.agent.IAgent;
import repastcity3.main.GlobalVars;

public class VehicleContext extends DefaultContext<IAgent>{
	
	public VehicleContext() {
		super(GlobalVars.CONTEXT_NAMES.VEHICLE_CONTEXT);
	}
	
}
