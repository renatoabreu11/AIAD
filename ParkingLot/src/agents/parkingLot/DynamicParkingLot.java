package agents.parkingLot;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import sajas.core.AID;
import utils.DailyInfo;
import utils.PricingScheme;
import utils.WeeklyInfo;

public class DynamicParkingLot extends ParkingLot {
	public DynamicParkingLot(Coordinate position, int maxCapacity) {
		super("DynamicParkingLot",position, maxCapacity,Type.DYNAMIC_PARKING_LOT);
	}

	public void updatePricingScheme() {
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
			if(variation > 2) {
				variation = 2;
			}
			
			if(variation < -2) {
				variation = -2;
			}
			
		    double learningRate = 0.3; 
		    double newMin = ps.getMinPricePerStay() + learningRate * ps.getMinPricePerStay() * (variation*100 - 1);
		    double newMax = ps.getMaxPricePerStay() + learningRate * ps.getMaxPricePerStay() * (variation*100 - 1);
		    double newCost = ps.getPricePerHour()+ learningRate * ps.getPricePerHour() * (variation - 1);
		    
		    previousWeeklyInfo = weeklyInfo;
		    weeklyInfo = new WeeklyInfo((AID) this.getAID(), new PricingScheme(newCost, newMin, newMax));
		    LOGGER.info("changed prices: " + newCost);
		}
	}
}
