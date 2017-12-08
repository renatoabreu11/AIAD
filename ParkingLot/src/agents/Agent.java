package agents;

import java.util.List;
import sajas.core.AID;

public abstract class Agent extends sajas.core.Agent{
	public static enum Type {
			EXPLORATORY_DRIVER,
			RATIONAL_DRIVER,
			DYNAMIC_PARKING_FACILITY,
			STATIC_PARKING_FACILITY,
			AGENT_MANAGER,
			MANAGER
	}
	
	public static int tick = 0;
	public static int UniqueID = 0;
	public Type type;
	
	public Agent(String name, Type type) {
		this.setAID(new AID(name + "#" + UniqueID, true));
		this.type = type;
		Agent.UniqueID++;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public static void updateTick() {
		++Agent.tick;
	}
}
