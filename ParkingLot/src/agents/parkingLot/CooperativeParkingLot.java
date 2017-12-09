package agents.parkingLot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;

import environment.Route;
import sajas.core.AID;
import utils.PricingScheme;
import utils.WeeklyInfo;

public class CooperativeParkingLot extends ParkingLot {
	public CooperativeParkingLot(Coordinate position, int maxCapacity,Coordinate currentPosition) {
		super(position, maxCapacity, currentPosition);
		this.type = Type.COOPERATIVE_PARKING_LOT;
	}

	public CooperativeParkingLot() {
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
	}

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
		
		previousWeeklyInfo = weeklyInfo;
		double[] pricesPerHour = new double[24];
		if(parksInfo.size() != 0) {
			int numberParks = parksInfo.size();
			for(WeeklyInfo wi : parksInfo) {
				double[] tmpPrices = wi.getPricingScheme().getPricesPerHour();
				for(int i = 0; i < tmpPrices.length; i++) {
					pricesPerHour[i] += tmpPrices[i]; 
				}
			}
			
			for(int i = 0; i < pricesPerHour.length; i++) {
				pricesPerHour[i] /= numberParks; 
			}
		} else {
			
		}
		
		PricingScheme ps = new PricingScheme(pricesPerHour);
		weeklyInfo = new WeeklyInfo((AID) this.getAID());
	}
}
