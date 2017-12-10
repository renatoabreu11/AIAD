package agents.driver;

import com.vividsolutions.jts.geom.Coordinate;

public class ExploratoryDriver extends Driver {
	public ExploratoryDriver(Coordinate srcPosition, Coordinate destPosition, int durationOfStay, double walkDistance, double defaultSatisfaction) {
		super("ExploratoryDriver",srcPosition, destPosition, durationOfStay, walkDistance, defaultSatisfaction, Type.EXPLORATORY_DRIVER);
	}
}
