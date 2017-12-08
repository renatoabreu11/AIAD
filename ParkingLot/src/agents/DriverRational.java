package agents;

import com.vividsolutions.jts.geom.Coordinate;

public class DriverRational extends Driver{
	public DriverRational(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super(srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction);
		this.type = Type.RATIONAL_DRIVER;
	}
}
