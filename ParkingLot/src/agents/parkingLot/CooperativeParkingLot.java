package agents.parkingLot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.vividsolutions.jts.geom.Coordinate;
import environment.Route;
import sajas.core.AID;
import utils.PricingScheme;
import utils.WeeklyInfo;

public class CooperativeParkingLot extends ParkingLot {
	public CooperativeParkingLot(Coordinate position, int maxCapacity) {
		super("CooperativeParkingLot",position, maxCapacity,Type.COOPERATIVE_PARKING_LOT);
		this.type = Type.COOPERATIVE_PARKING_LOT;
	}

	/*public CooperativeParkingLot() {
		super("CooperativeParkingLot", Type.COOPERATIVE_PARKING_LOT);
		double[] pricesPerHour = new double[24];
		Random generator = new Random();
	    for(int i=0; i < pricesPerHour.length; i++)
	    {
	    	double number = generator.nextDouble();
	    	pricesPerHour[i] = number;
	    }
	    PricingScheme ps = new PricingScheme(pricesPerHour);
	    this.weeklyInfo.setPricingScheme(ps);
	}*/

	public void updatePricingScheme(HashMap<String, WeeklyInfo> parksWeeklyInfo) {

		ArrayList<WeeklyInfo> parksInfo = new ArrayList<>();
		for (Map.Entry<String, WeeklyInfo> entry : parksWeeklyInfo.entrySet()) {
		    WeeklyInfo wi = entry.getValue();
		    
		    double[] distAndAng = new double[2];
	    	Route.distance(this.getPosition(), wi.getParkingLotPosition(), distAndAng);
		    
	    	if(wi.getProfitPerDriver() > this.weeklyInfo.getProfitPerDriver() && distAndAng[0] < 400) { // Change value later
	    		parksInfo.add(wi);
	    	}
		}	
		
		double[] pricesPerHour = new double[24];
		double newCost = 0, newMin = 0, newMax = 0;
		if(parksInfo.size() != 0) {
			int numberParks = parksInfo.size();
			for(WeeklyInfo wi : parksInfo) {
				double[] tmpPrices = wi.getPricingScheme().getPricesPerHour();
				for(int i = 0; i < tmpPrices.length; i++) {
					pricesPerHour[i] += tmpPrices[i]; 
				}
				newCost += wi.getPricingScheme().getPricePerHour();
				newMin += wi.getPricingScheme().getMinPricePerStay();
				newMax += wi.getPricingScheme().getMaxPricePerStay();
			}
			for(int i = 0; i < pricesPerHour.length; i++) {
				pricesPerHour[i] /= numberParks; 
			}
			newCost /= parksInfo.size();
			newMin /= parksInfo.size();
			newMax /= parksInfo.size();
		} else {
			double previousProfit = previousWeeklyInfo.getTotalProfit();
			double[] previousPrices = previousWeeklyInfo.getPricingScheme().getPricesPerHour();
			double variation;
			if(previousProfit == 0) variation = weeklyInfo.getTotalProfit();
			else variation = weeklyInfo.getTotalProfit() / previousProfit;
			if(variation > 2) {
				variation = 2;
			}
			if(variation < -2) {
				variation = -2;
			}
			
			double learningRate = 0.3; 
			double[] prices = weeklyInfo.getPricingScheme().getPricesPerHour();
			for (int i = 0; i < pricesPerHour.length; i++) {
				pricesPerHour[i] = prices[i] + learningRate * prices[i] * (variation - 1);
			}
			newMin = weeklyInfo.getPricingScheme().getMinPricePerStay() + learningRate * weeklyInfo.getPricingScheme().getMinPricePerStay() * (variation - 1);
		    newMax = weeklyInfo.getPricingScheme().getMaxPricePerStay() + learningRate * weeklyInfo.getPricingScheme().getMaxPricePerStay() * (variation - 1);
		    newCost = weeklyInfo.getPricingScheme().getPricePerHour()+ learningRate * weeklyInfo.getPricingScheme().getPricePerHour() * (variation - 1);
			
		}
		previousWeeklyInfo = weeklyInfo;
		weeklyInfo = new WeeklyInfo((AID) this.getAID(), new PricingScheme(pricesPerHour));
	}
}
