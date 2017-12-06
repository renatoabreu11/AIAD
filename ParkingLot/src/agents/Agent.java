package agents;

import java.util.List;

public abstract class Agent extends sajas.core.Agent{
	public static enum Type {
			EXPLORATORY_DRIVER,
			RATIONAL_DRIVER,
			DYNAMIC_PARKING_FACILITY,
			STATIC_PARKING_FACILITY
	}
	public static int tick = 0;
	
	public static int UniqueID = 0;
	public Type type;
	
	public Agent(String type) {
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
	
	
	public List<String> getTransportAvailable() {
		return null;
	}
}
