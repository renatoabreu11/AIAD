package agents;

import com.vividsolutions.jts.geom.Coordinate;

public class ParkingLotDynamic extends ParkingLot {
	public ParkingLotDynamic(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.DYNAMIC_PARKING_FACILITY;
	}
	
	public void updatePrice(int nextDay) {
		int currentDay = (nextDay-1 + 7) % 7;
		double variation = profit / profits[currentDay];
		double learningRate = 0.3;
		prices[currentDay] += learningRate * prices[currentDay] * (variation - 1);
		profit = 0;
		pricePerMinute = prices[nextDay];
		
		LOGGER.severe(this.UniqueID + " set new price for weekday " + currentDay + ": " + prices[currentDay]);
	}
}
