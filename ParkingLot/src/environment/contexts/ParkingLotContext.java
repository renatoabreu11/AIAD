package environment.contexts;

import repast.simphony.context.DefaultContext;
import agents.Agent;
import agents.parkingLot.ParkingLot;
import parkingLot.GlobalVars;

public class ParkingLotContext extends DefaultContext<ParkingLot>{
	public ParkingLotContext() {
		super(GlobalVars.CONTEXT_NAMES.PARKINGLOT_CONTEXT);
	}
}
