package agents;

import com.vividsolutions.jts.geom.Coordinate;

public class DriverExplorer extends Driver {
	public DriverExplorer(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super(srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction);
		this.type = Type.EXPLORATORY_DRIVER;
	}
}
