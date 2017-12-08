package agents.parkingLot;

import com.vividsolutions.jts.geom.Coordinate;

public class CooperativeParkingLot extends ParkingLot {
	public CooperativeParkingLot(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.COOPERATIVE_PARKING_LOT;
	}

	public CooperativeParkingLot() {
		super("CooperativeParkingLot", Type.COOPERATIVE_PARKING_LOT);
	}

	public void storeExternalParkInfo(String name) {
		// TODO Auto-generated method stub
		
	}
}
