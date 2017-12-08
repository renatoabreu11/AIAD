package agents;

import com.vividsolutions.jts.geom.Coordinate;

public class ParkingLotDynamic extends ParkingLot {
	public ParkingLotDynamic(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.STATIC_PARKING_FACILITY;
	}
}
