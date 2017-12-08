package parkingLot;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import agents.Agent;
import agents.AgentManager;
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
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;

public class Initializer extends RepastSLauncher{
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	private static long speedTimer = -1;
	
	private static ContainerController mainContainer;
	public static Context context;
	private AgentManager agentManager;
	private Simulation simulation;
	
	public static Context<Agent> agentContext;
	private static Geography<Agent> agentGeography;
	
	public static Context<ParkingLot> parkingLotContext;
	private static Geography<ParkingLot> parkingLotGeography;
	
	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;
	
	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	
	public static Network<Junction> roadNetwork;

	@Override
	public String getName() {
		return "ParkingLotSimulation";
	}

	@Override
	protected void launchJADE() {
		LOGGER.info("Launching JADE wrapper");
		sajas.core.Runtime rt = sajas.core.Runtime.instance();
		
		simulation = new Simulation();
		Profile profile = new ProfileImpl();
		mainContainer = rt.createMainContainer(profile);
		
		agentManager = new AgentManager(mainContainer);
		
		try {
			mainContainer.acceptNewAgent(agentManager.getName(), agentManager).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		agentManager.initAgents(params);
		simulation.AddAgentsToEnvironent(agentManager.parkingAgents, agentManager.driverAgents);
		simulation.scheduleSimulation(this);
		agentManager.startAgents();
		
	}
	
	@Override
	public Context<?> build(Context<Object> context) {
		context.setId("ParkingLotSimulation");
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
		
		Initializer.context = context;
		return super.build(context);
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
	public static synchronized void moveAgentByVector(Agent agent, double distToTravel, double angle) {
		agentGeography.moveByVector(agent, distToTravel, angle);
	}
	
	/**
	 * Get the geometry of the given agent. This method is required -- rather than giving agents direct access to the
	 * agentGeography -- because when multiple threads are used they can interfere with each other and agents end up
	 * moving incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(Agent agent) {
		return Initializer.agentGeography.getGeometry(agent);
	}
	
	public static synchronized Geography<Agent> getAgentGeography() {
		return agentGeography;
	}
	
	public static synchronized Geography<ParkingLot> getParkingLotGeography() {
		return parkingLotGeography;
	}
	
	/**
	 * Print information about each iteration
	 */
	public void printTicks() {
		/*LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+
				". Speed: "+((double)(System.currentTimeMillis()-speedTimer)/1000.0)+
				"sec/ticks.");*/
		speedTimer = System.currentTimeMillis();
	}
}