package agents.parkingLot;

import com.vividsolutions.jts.geom.Coordinate;

import sajas.core.AID;
import utils.WeeklyInfo;

public class StaticParkingLot extends ParkingLot {
	public StaticParkingLot(Coordinate position, int maxCapacity) {
		super("StaticParkingLot",position, maxCapacity,Type.STATIC_PARKING_LOT);
	}
	
	public void updatePricingScheme() {    
		previousWeeklyInfo = weeklyInfo;
		weeklyInfo = new WeeklyInfo((AID) this.getAID());
	}
}
