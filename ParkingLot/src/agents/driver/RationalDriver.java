package agents.driver;

import com.vividsolutions.jts.geom.Coordinate;

import agents.Agent.Type;

public class RationalDriver extends Driver{
	public RationalDriver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super(srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction);
		this.type = Type.RATIONAL_DRIVER;
	}
	
	public RationalDriver() {
		super("RationalDriver", Type.RATIONAL_DRIVER);
	}
}
