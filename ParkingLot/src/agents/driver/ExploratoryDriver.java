package agents.driver;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Coordinate;
import agents.parkingLot.ParkingLot;
import environment.Route;
import parkingLot.Initializer;

public class ExploratoryDriver extends Driver {
	public ExploratoryDriver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super("ExploratoryDriver",srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction, Type.EXPLORATORY_DRIVER);
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
				ParkComparable pc = new ParkComparable(parks.get(i).getAID().toString());
				pc.setDistance(distAndAng[0]);
				parksInRange.add(pc);	
			}
		}
	}

	@Override
	void updatePossibleParks() {}
}
