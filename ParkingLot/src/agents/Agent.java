package agents;

import sajas.core.AID;

public abstract class Agent extends sajas.core.Agent{
	public static enum Type {
			EXPLORATORY_DRIVER,
			RATIONAL_DRIVER,
			DYNAMIC_PARKING_LOT,
			STATIC_PARKING_LOT,
			AGENT_MANAGER,
			MANAGER,
			COOPERATIVE_PARKING_LOT
	}
	
	public static int tick = 0;
	public static int UniqueID = 0;
	public Type type;
	
	public Agent(String name, Type type) {
		this.setAID(new AID(name + "#" + UniqueID, true));
		this.type = type;
		Agent.UniqueID++;
	}
	
	public Agent(String name) {
		this.setAID(new AID(name + "#" + UniqueID, true));
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
