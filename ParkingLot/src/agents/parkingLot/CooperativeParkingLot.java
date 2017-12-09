package agents.parkingLot;

import java.util.HashMap;

import com.vividsolutions.jts.geom.Coordinate;

import utils.WeeklyInfo;

public class CooperativeParkingLot extends ParkingLot {
	public CooperativeParkingLot(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.COOPERATIVE_PARKING_LOT;
	}

	public CooperativeParkingLot() {
		super("CooperativeParkingLot", Type.COOPERATIVE_PARKING_LOT);
	}

	public void updatePricingScheme(HashMap<String, WeeklyInfo> parksWeeklyInfo) {
		// TODO Auto-generated method stub
	}
}
