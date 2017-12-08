package parkingLot;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import agents.Driver;
import agents.IAgent;
import agents.ParkingLot;
import environment.GISFunctions;
import environment.Junction;
import environment.NetworkEdgeCreator;
import environment.Road;
import environment.SpatialIndexManager;
import environment.contexts.AgentContext;
import environment.contexts.JunctionContext;
import environment.contexts.RoadContext;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;

public class Initializer implements ContextBuilder<Object> {
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	
	public static Context<IAgent> agentContext;
	private static Geography<IAgent> agentGeography;
	
	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;
	
	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	
	public static Network<Junction> roadNetwork;
	
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("ParkingLot");

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

		agentContext = new AgentContext();
		context.addSubContext(agentContext);
		agentGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
				GlobalVars.CONTEXT_NAMES.AGENT_GEOGRAPHY, agentContext,
				new GeographyParameters<IAgent>(new SimpleAdder<IAgent>()));
		
		Junction junction;
		Road road;
		Point point;
		ParkingLot p;
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int zombieCount = (Integer) params.getValue("parking_count");
		//TODO change cycle to iterable of randomObject[]
		for (int i = 0; i < zombieCount; i++) {
			p = new ParkingLot();
			junction = junctionContext.getRandomObject();
			point = junctionGeography.getGeometry(junction).getCentroid();
			agentContext.add(p);
			agentGeography.move(p,  point);
		}

		int humanCount = (Integer) params.getValue("driver_count");
		for (int i = 0; i < humanCount; i++) {
			Driver driver = new Driver();
			road = roadContext.getRandomObject();
			ArrayList<Junction> endpoints = road.getJunctions();
			point = junctionGeography.getGeometry(endpoints.get(0)).getCentroid();
			agentContext.add(driver);
			agentGeography.move(driver, point);
		}
		
		Manager manager = new Manager();
		agentContext.add(manager);
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20);
		}
		
//		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
//
//		// Schedule something that outputs ticks every 1000 iterations.
//		schedule.schedule(ScheduleParameters.createRepeating(1, 1000, ScheduleParameters.LAST_PRIORITY), this,
//				"printTicks");
//		ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
//		// Schedule the agents' step methods.
//		for (IAgent a : agentContext.getObjects(IAgent.class)) {
//			schedule.schedule(agentStepParams, a, "update");
//		}

		return context;
	}
	
	private static long speedTimer = -1; // For recording time per N iterations 
	public void printTicks() {
//		LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+
//				". Speed: "+((double)(System.currentTimeMillis()-speedTimer)/1000.0)+
//				"sec/ticks.");
		speedTimer = System.currentTimeMillis();
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
	public static synchronized void moveAgent(IAgent agent, Point point) {
		Initializer.agentGeography.move(agent, point);
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
	public static synchronized void moveAgentByVector(IAgent agent, double distToTravel, double angle) {
		agentGeography.moveByVector(agent, distToTravel, angle);
	}
	
	/**
	 * Get the geometry of the given agent. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(IAgent agent) {
		return Initializer.agentGeography.getGeometry(agent);
	}
	
	public static Geography<IAgent> getAgentGeography() {
		return agentGeography;
	}
	
	public static void removeAgent(IAgent agent) {
		
		agentContext.remove(agent);
		agentGeography.move(agent, null);
	}
}