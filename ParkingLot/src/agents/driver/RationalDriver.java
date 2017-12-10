package agents.driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import com.vividsolutions.jts.geom.Coordinate;
import agents.parkingLot.ParkingLot;
import environment.Route;
import parkingLot.Initializer;

public class RationalDriver extends Driver{
	public RationalDriver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super("RationalDriver",srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction,Type.RATIONAL_DRIVER);
	}

	@Override
	void getPossibleParks() {
		Coordinate tmp = new Coordinate();
		double[] distAndAng = new double[2];
		ArrayList<ParkingLot> parks = Initializer.agentManager.getParkingAgents();

		for(int i = 0; i < parks.size(); i++) {
			tmp = parks.get(i).getPosition();
			Route.distance(this.destination, tmp, distAndAng);
			if(distAndAng[0] < this.walkDistance) {
				double price = parks.get(i).getFinalPrice(String.valueOf(this.getDurationOfStay()));
				double utility = this.getUtility(price);
				ParkComparable pc = new ParkComparable(parks.get(i).getAID().toString());
				pc.setUtility(utility);
				parksInRange.add(pc);
			}
		}
		sortParksInRange();
	}

	@Override
	void updatePossibleParks() {
		for(ParkComparable pc : parksInRange) {
			ParkingLot pl = Initializer.agentManager.getAgent(pc.park);
			double price = pl.getFinalPrice(String.valueOf(this.getDurationOfStay()));
			double utility = this.getUtility(price);
			pc.setUtility(utility);
		}
		sortParksInRange();
	}
	
	private void sortParksInRange() {
		Collections.sort(parksInRange, new Comparator<ParkComparable>() {
	        @Override
	        public int compare(ParkComparable pc1, ParkComparable pc2)
	        {
	        	if (pc1.utility < pc2.utility) return 1;
	            if (pc1.utility > pc2.utility) return -1;
	            return 0;
	        }
	    });
	}
}
