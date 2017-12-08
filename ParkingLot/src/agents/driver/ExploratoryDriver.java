package agents.driver;

import com.vividsolutions.jts.geom.Coordinate;

import agents.Agent.Type;

public class ExploratoryDriver extends Driver {
	public ExploratoryDriver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super(srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction);
		this.type = Type.EXPLORATORY_DRIVER;
	}
	
	public ExploratoryDriver() {
		super("ExploratoryDriver", Type.EXPLORATORY_DRIVER);
	}
}
