package agents;

import java.util.ArrayList;
import java.util.List;

public class ParkingLot extends IAgent {
	
	public void update() {};

	
	/** A list of agents who live here */
	private List<Agent> agents;

	public ParkingLot() {
		this.agents = new ArrayList<Agent>();
	}
	

	public void addAgent(Agent a) {
		this.agents.add(a);
	}

	public List<Agent> getAgents() {
		return this.agents;
	}
}
