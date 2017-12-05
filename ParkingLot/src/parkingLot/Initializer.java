package parkingLot;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import agents.IAgent;
import agents.Driver;
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
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;

public class Initializer implements ContextBuilder<Object> {
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	
	public static Context<IAgent> agentContext;
	public static Geography<IAgent> agentGeography;


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
			
			GISFunctions.readShapefile(Road.class, "./data/espinho.shp", roadProjection, roadContext);
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
		
		Driver driver = new Driver();
		Road road = roadContext.getRandomObject();
		ArrayList<Junction> endpoints = road.getJunctions();
		Coordinate[] c = roadProjection.getGeometry(road).getCoordinates();
		
		Point p = junctionGeography.getGeometry(endpoints.get(0)).getCentroid();
		agentContext.add(driver);
		agentGeography.move(driver, p);
		
		Junction j = junctionContext.getRandomObject();
		ShortestPath<Junction> shortest = new ShortestPath<Junction>(roadNetwork);
		List<RepastEdge<Junction>> shortestPath = shortest.getPath(endpoints.get(0), endpoints.get(1));
		//double pathLength = shortest.getPathLength(endpoints.get(0), j);
		driver.junctionsToPass = shortestPath;
		driver.currentJunction = 0;
		driver.currentRoad = road;
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20);
		}
		
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

		// Schedule something that outputs ticks every 1000 iterations.
		schedule.schedule(ScheduleParameters.createRepeating(1, 1000, ScheduleParameters.LAST_PRIORITY), this,
				"printTicks");
		ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 0);
		// Schedule the agents' step methods.
		for (IAgent a : agentContext.getObjects(IAgent.class)) {
			schedule.schedule(agentStepParams, a, "step");
		}

		return context;
	}
	
	private static long speedTimer = -1; // For recording time per N iterations 
	public void printTicks() {
		LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+
				". Speed: "+((double)(System.currentTimeMillis()-speedTimer)/1000.0)+
				"sec/ticks.");
		speedTimer = System.currentTimeMillis();
	}
}