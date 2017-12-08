package behaviours;

import agents.parkingLot.ParkingLot;
import sajas.core.Agent;
import sajas.core.behaviours.TickerBehaviour;

public class DailyUpdatePerformer extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2872069012274382797L;

	/**
	 * Daily behavior that saves the daily park info
	 * @param a
	 * @param period
	 */
	public DailyUpdatePerformer(Agent a, long period) {
		super(a, period);
	}

	@Override
	protected void onTick() {
		((ParkingLot) myAgent).saveDailyInfo();
	}
}