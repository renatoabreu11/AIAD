package parkingLot;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import agents.Agent;
import agents.Driver;
import agents.ParkingLot;
import environment.Junction;
import environment.Road;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;

public class Simulation {
	private static Logger LOGGER = Logger.getLogger(Simulation.class.getName());

	public Simulation() { }
	
	/**
	 * Add the defined agents to the simulation
	 */
	public void AddAgentsToEnvironent(ArrayList<ParkingLot> parkingLotAgents, ArrayList<Driver> driverAgents) {
		System.out.println(parkingLotAgents);
		Junction junction;
		Road road;
		Point point;
		
		for (int i = 0; i < parkingLotAgents.size(); i++) {
			ParkingLot pl = parkingLotAgents.get(i);
			junction = Initializer.junctionContext.getRandomObject();
			point = Initializer.junctionGeography.getGeometry(junction).getCentroid();
			pl.setPosition(new Coordinate(point.getX(),point.getY()));
			
			Initializer.agentContext.add(pl);
			Initializer.getAgentGeography().move(pl, point);
			Initializer.parkingLotContext.add(pl);
			Initializer.getParkingLotGeography().move(pl,  point);
		}

		for (int i = 0; i < driverAgents.size(); i++) {
			Driver driver = driverAgents.get(i);
			road = Initializer.roadContext.getRandomObject();
			ArrayList<Junction> endpoints = road.getJunctions();
			Point initialPoint = Initializer.junctionGeography.getGeometry(endpoints.get(0)).getCentroid();
			
			junction = Initializer.junctionContext.getRandomObject();
			Point finalPoint = Initializer.junctionGeography.getGeometry(junction).getCentroid();
			
			Coordinate initialCoordinate = new Coordinate(initialPoint.getX(),initialPoint.getY());
			Coordinate finalCoordinate = new Coordinate(finalPoint.getX(),finalPoint.getY());
			
			driver.setPositions(initialCoordinate, finalCoordinate);
		
			Initializer.agentContext.add(driver);
			Initializer.getAgentGeography().move(driver, initialPoint);
		}
		
	}
	
	/**
	 * Schedule the simulation and defines the update method for each agent context
	 */
	public void scheduleSimulation(Initializer initializer) {
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20);
		}
		
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		schedule.schedule(ScheduleParameters.createRepeating(1, 1000, ScheduleParameters.LAST_PRIORITY), initializer,
				"printTicks");
		ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
		for (Agent a : Initializer.agentContext.getObjects(Agent.class)) {
			schedule.schedule(agentStepParams, a, "update");
		}
	}
}
