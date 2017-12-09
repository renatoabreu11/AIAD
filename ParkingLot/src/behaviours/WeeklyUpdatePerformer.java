package behaviours;

import agents.Agent.Type;
import agents.parkingLot.DynamicParkingLot;
import agents.parkingLot.ParkingLot;
import sajas.core.behaviours.OneShotBehaviour;

public class WeeklyUpdatePerformer extends OneShotBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7618648962155948099L;

	/**
	 * Weekly behavior that saves the weekly park info
	 * @param a
	 * @param period
	 */
	@Override
	public void action() {
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
