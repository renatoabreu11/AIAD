package agents.parkingLot;

import com.vividsolutions.jts.geom.Coordinate;

public class StaticParkingLot extends ParkingLot {
	public StaticParkingLot(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.STATIC_PARKING_LOT;
	}
	
	public StaticParkingLot() {
		super("StaticParkingLot", Type.STATIC_PARKING_LOT);
	}
}
