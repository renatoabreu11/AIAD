package agents;

import com.vividsolutions.jts.geom.Coordinate;

public class ParkingLotStatic extends ParkingLot {
	public ParkingLotStatic(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.STATIC_PARKING_FACILITY;
	}
}
