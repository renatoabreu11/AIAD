package agents;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class ParkingLot extends IAgent {
	
	private Coordinate currentPosition;
	
	public void update() {};

	
	/** A list of agents who live here */
	private List<IAgent> agents;

	public ParkingLot(Coordinate currentPosition) {
		this.agents = new ArrayList<IAgent>();
		this.currentPosition = currentPosition;
	}
	
	public Coordinate getCurrentPosition() {
		return currentPosition;
	}
	

	public void addAgent(IAgent a) {
		this.agents.add(a);
	}

	public List<IAgent> getAgents() {
		return this.agents;
	}
}
