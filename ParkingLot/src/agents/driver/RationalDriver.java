package agents.driver;

import com.vividsolutions.jts.geom.Coordinate;

public class RationalDriver extends Driver{
	public RationalDriver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super("RationalDriver",srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction,Type.RATIONAL_DRIVER);
	}
}
