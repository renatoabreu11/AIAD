package parkingLot;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import agents.Agent;
import agents.Driver;
import agents.ParkingLot;
import environment.GISFunctions;
import environment.Junction;
import environment.NetworkEdgeCreator;
import environment.Road;
import environment.SpatialIndexManager;
import environment.contexts.AgentContext;
import environment.contexts.JunctionContext;
import environment.contexts.ParkingLotContext;
import environment.contexts.RoadContext;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;

public class Simulation {
	private static Logger LOGGER = Logger.getLogger(Simulation.class.getName());
	public static Context<Object> context;

	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;

	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;

	public static Network<Junction> roadNetwork;

	public static Context<Agent> agentContext;
	private static Geography<Agent> agentGeography;

	public static Context<ParkingLot> parkingLotContext;
	private static Geography<ParkingLot> parkingLotGeography;

	public Simulation(Context<Object> context) {
		Simulation.context = context;
	}

	/**
	 * Add environment to the simulation
	 */
	public void addEnvironment() {
		try {
			roadContext = new RoadContext();
			roadProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.ROAD_GEOGRAPHY, roadContext,
					new GeographyParameters<Road>(new SimpleAdder<Road>()));

			GISFunctions.readShapefile(Road.class, "./data/roads.shp", roadProjection, roadContext);
			context.addSubContext(roadContext);
			SpatialIndexManager.createIndex(roadProjection, Road.class);

			// Create road network
			// 1.junctionContext and junctionGeography
			junctionContext = new JunctionContext();
			context.addSubContext(junctionContext);
			junctionGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					GlobalVars.CONTEXT_NAMES.JUNCTION_GEOGRAPHY, junctionContext,
					new GeographyParameters<Junction>(new SimpleAdder<Junction>()));

			// 2. roadNetwork
			NetworkBuilder<Junction> builder = new NetworkBuilder<Junction>(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK,
					junctionContext, false);
			builder.setEdgeCreator(new NetworkEdgeCreator<Junction>());
			roadNetwork = builder.buildNetwork();
			GISFunctions.buildGISRoadNetwork(roadProjection, junctionContext, junctionGeography, roadNetwork);

			// Add the junctions to a spatial index (couldn't do this until the
			// road network had been created).
			SpatialIndexManager.createIndex(junctionGeography, Junction.class);

		} catch (MalformedURLException | FileNotFoundException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Add agents (driver and parks) to the context
	 */
	public void addAgentsToContext() {
		// creates agent context
		agentContext = new AgentContext();
		context.addSubContext(agentContext);
		agentGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
				GlobalVars.CONTEXT_NAMES.AGENT_GEOGRAPHY, agentContext,
				new GeographyParameters<Agent>(new SimpleAdder<Agent>()));

		//creates parking context to have access to only parking lot agents
		parkingLotContext = new ParkingLotContext();
		context.addSubContext(parkingLotContext);
		parkingLotGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
				GlobalVars.CONTEXT_NAMES.PARKINGLOT_GEOGRAPHY, parkingLotContext,
				new GeographyParameters<ParkingLot>(new SimpleAdder<ParkingLot>()));

	}

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
			junction = junctionContext.getRandomObject();
			point = junctionGeography.getGeometry(junction).getCentroid();
			pl.setPosition(new Coordinate(point.getX(),point.getY()));

			agentContext.add(pl);
			getAgentGeography().move(pl, point);
			parkingLotContext.add(pl);
			getParkingLotGeography().move(pl,  point);
		}

		for (int i = 0; i < driverAgents.size(); i++) {
			Driver driver = driverAgents.get(i);
			road = roadContext.getRandomObject();
			ArrayList<Junction> endpoints = road.getJunctions();
			Point initialPoint = junctionGeography.getGeometry(endpoints.get(0)).getCentroid();

			junction = junctionContext.getRandomObject();
			Point finalPoint = junctionGeography.getGeometry(junction).getCentroid();

			Coordinate initialCoordinate = new Coordinate(initialPoint.getX(),initialPoint.getY());
			Coordinate finalCoordinate = new Coordinate(finalPoint.getX(),finalPoint.getY());

			driver.setPositions(initialCoordinate, finalCoordinate);

			agentContext.add(driver);
			getAgentGeography().move(driver, initialPoint);
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
		for (Agent a : agentContext.getObjects(Agent.class)) {
			schedule.schedule(agentStepParams, a, "update");
		}
	}

	/**
	 * Remove an agent from the simulation and scheduler
	 * @param driver
	 */
	public static void removeAgent(Driver driver) {
		agentContext.remove(driver);
		agentGeography.move(driver, null);
	}
	
	/**
	 * Move an agent. This method is required -- rather than giving agents direct access to the agentGeography --
	 * because when multiple threads are used they can interfere with each other and agents end up moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param point
	 *            The point to move the agent to
	 */
	public static synchronized void moveAgent(Agent agent, Point point) {
		agentGeography.move(agent, point);
	}
	
	/**
	 * Move an agent by a vector. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 * 
	 * @param agent
	 *            The agent to move.
	 * @param distToTravel
	 *            The distance that they will travel
	 * @param angle
	 *            The angle at which to travel.
	 * @see Geography
	 */
	public static synchronized void moveAgentByVector(Agent agent, double distToTravel, double angle) {
		agentGeography.moveByVector(agent, distToTravel, angle);
	}
	
	/**
	 * Get the geometry of the given agent. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(Agent agent) {
		return agentGeography.getGeometry(agent);
	}
	
	/**
	 * Get the agent geometry
	 * @return
	 */
	public static synchronized Geography<Agent> getAgentGeography() {
		return agentGeography;
	}
	
	/**
	 * Get the parking lot geometry
	 * @return
	 */
	public static synchronized Geography<ParkingLot> getParkingLotGeography() {
		return parkingLotGeography;
	}
	
	/**
	 * Return the simulation context
	 * @return
	 */
	public Context<Object> getContext() {
		return Simulation.context;
	}
}
