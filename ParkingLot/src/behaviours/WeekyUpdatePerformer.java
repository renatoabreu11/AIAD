package behaviours;

import agents.Agent.Type;
import agents.parkingLot.DynamicParkingLot;
import agents.parkingLot.ParkingLot;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

public class WeekyUpdatePerformer extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7618648962155948099L;

	public WeekyUpdatePerformer(Agent a, long period) {
		super(a, period);
	}

	@Override
	protected void onTick() {
		((ParkingLot) myAgent).closeParkingFacility();
		((ParkingLot) myAgent).saveWeeklyInfo();
		Type type = ((ParkingLot) myAgent).type;
		if(type.equals(Type.DYNAMIC_PARKING_LOT))
			((DynamicParkingLot) myAgent).updatePricingSheme();
		else if(type.equals(Type.COOPERATIVE_PARKING_LOT)) {
			myAgent.addBehaviour(new ShareWeeklyInfoPerformer());
		}
	}
}
