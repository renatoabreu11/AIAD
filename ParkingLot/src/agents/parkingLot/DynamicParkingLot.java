package agents.parkingLot;

import com.vividsolutions.jts.geom.Coordinate;

public class DynamicParkingLot extends ParkingLot {
	public DynamicParkingLot(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.DYNAMIC_PARKING_LOT;
	}

	public DynamicParkingLot() {
		super("DynamicParkingLot", Type.DYNAMIC_PARKING_LOT);
	}

	public void updatePricingSheme() {
		// TODO Auto-generated method stub
		
	}
}
