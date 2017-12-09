package agents.parkingLot;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import sajas.core.AID;
import utils.DailyInfo;
import utils.PricingScheme;
import utils.WeeklyInfo;

public class DynamicParkingLot extends ParkingLot {
	public DynamicParkingLot(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.DYNAMIC_PARKING_LOT;
	}

	public DynamicParkingLot() {
		super("DynamicParkingLot", Type.DYNAMIC_PARKING_LOT);
	}

	public void updatePricingSheme() {
		if(previousWeeklyInfo == null) {
			previousWeeklyInfo = weeklyInfo;
		} else {
			PricingScheme ps = weeklyInfo.getPricingScheme();
			ArrayList<DailyInfo> days = weeklyInfo.getDailyInfo();
			ArrayList<DailyInfo> previousDays = previousWeeklyInfo.getDailyInfo();
			double variation = 0;
			
			for (int i = 0; i < days.size(); i++) {
				if(previousDays.get(i).getTotalProfit() != 0)
					variation += days.get(i).getTotalProfit() / previousDays.get(i).getTotalProfit();
				else
					variation += days.get(i).getTotalProfit();
			}
			variation /= 7; 
			 
		    double learningRate = 0.3; 
		    double newMin = ps.getMinPricePerStay() + learningRate * ps.getMinPricePerStay() * (variation - 1);
		    double newMax = ps.getMaxPricePerStay() + learningRate * ps.getMaxPricePerStay() * (variation - 1);
		    double newCost = ps.getPricePerMinute()+ learningRate * ps.getPricePerMinute() * (variation - 1);
		    
		    previousWeeklyInfo = weeklyInfo;
		    weeklyInfo = new WeeklyInfo((AID) this.getAID(), new PricingScheme(newCost, newMin, newMax));
		}
	}
}
