package parkingLot;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import agents.Agent;
import agents.driver.Driver;
import agents.driver.ExploratoryDriver;
import agents.driver.RationalDriver;
import agents.parkingLot.DynamicParkingLot;
import agents.parkingLot.ParkingLot;
import agents.parkingLot.StaticParkingLot;
import environment.GISFunctions;
import environment.Junction;
import environment.NetworkEdgeCreator;
import environment.Road;
import environment.SpatialIndexManager;
import environment.contexts.AgentContext;
import environment.contexts.JunctionContext;
import environment.contexts.ParkingLotContext;
import environment.contexts.RoadContext;
import repast.simphony.parameter.Parameters;
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
	public void addAgentsToContext(Manager manager) {
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

		agentContext.add(manager);
	}

	/**
	 * Add the defined agents to the simulation
	 */
	public void AddAgentsToEnvironent(ArrayList<ParkingLot> parkingLotAgents, ArrayList<Driver> driverAgents,Parameters params) {
		int nrParkingAgents = 8;//params.getInteger("parking_count");
		
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		schedule.schedule(ScheduleParameters.createRepeating(250, 250), this, //começa aos 250 ticks e chama a função addDrivers de 250 em 250
				"addDrivers");
		
		this.addParkingLots(nrParkingAgents, parkingLotAgents);
	}
	
	public void addParkingLots(int nrParkingAgents,ArrayList<ParkingLot> parkingLotAgents) {
		Junction junction;
		Point point;
		Random r = new Random();
		int type = 0;
		int parkLotation;
		
		StaticParkingLot sPark;
		DynamicParkingLot dPark;
		for(int i = 0; i < nrParkingAgents; i++) {
			type = r.nextInt(2);
			parkLotation = r.nextInt(101)+250;
			junction = junctionContext.getRandomObject();
			point = junctionGeography.getGeometry(junction).getCentroid();
			if(type == 0) {
				System.out.println("Static"+i);
				sPark = new StaticParkingLot(new Coordinate(point.getX(),point.getY()),parkLotation);
				agentContext.add(sPark);
				getAgentGeography().move(sPark, point);
				parkingLotContext.add(sPark);
				getParkingLotGeography().move(sPark,  point);
				
				parkingLotAgents.add(sPark);
			} else {
				System.out.println("Dynamic"+i);
				dPark = new DynamicParkingLot(new Coordinate(point.getX(),point.getY()),parkLotation);
				agentContext.add(dPark);
				getAgentGeography().move(dPark, point);
				parkingLotContext.add(dPark);
				getParkingLotGeography().move(dPark,  point);
				
				parkingLotAgents.add(dPark);
			}
			
			System.out.println("X: " +point.getX()+" ; Y: "+point.getY()+" "+parkLotation);
		}
	}

	public void addDrivers() {
		Random r = new Random();
		int nrDriverAgents;
		
		double offset = (r.nextInt(30)+85)/100;
		if(Initializer.manager.getDay() == 6 || Initializer.manager.getDay() == 7) {
			nrDriverAgents = (int) ((int) Initializer.manager.calculateNumberOfDriversWeekEndDays()*offset);
		} else {
			nrDriverAgents = (int) ((int) Initializer.manager.calculateNumberOfDriversWeekDays()*offset);
		}
		
		Junction junction;
		Road road;
		int type = 0;
		
		ExploratoryDriver eDriver;
		RationalDriver rDriver;
		for (int i = 0; i < nrDriverAgents; i++) {
			type = r.nextInt(2);
			road = roadContext.getRandomObject();
			ArrayList<Junction> endpoints = road.getJunctions();
			Point initialPoint = junctionGeography.getGeometry(endpoints.get(0)).getCentroid();
			junction = junctionContext.getRandomObject();
			Point finalPoint = junctionGeography.getGeometry(junction).getCentroid();
			Coordinate initialCoordinate = new Coordinate(initialPoint.getX(),initialPoint.getY());
			Coordinate finalCoordinate = new Coordinate(finalPoint.getX(),finalPoint.getY());
			int durationOfStay = (r.nextInt(100)+1)*10; // [10,10000]
			double walkDistance = (r.nextInt(500)+100); // [100,600[
			double defaultSatisfaction = 1.0; //TODO random satisfaction
			
			if(type == 0) {
				eDriver = new ExploratoryDriver(initialCoordinate,finalCoordinate,durationOfStay,walkDistance,defaultSatisfaction);
				agentContext.add(eDriver);
				getAgentGeography().move(eDriver, initialPoint);
				Initializer.agentManager.acceptDriver(eDriver);
			} else {
				rDriver = new RationalDriver(initialCoordinate,finalCoordinate,durationOfStay,walkDistance,defaultSatisfaction);
				agentContext.add(rDriver);
				getAgentGeography().move(rDriver, initialPoint);
				Initializer.agentManager.acceptDriver(rDriver);
			}
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
