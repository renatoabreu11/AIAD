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
import agents.parkingLot.CooperativeParkingLot;
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
import parkingLot.Initializer.ExperienceType;
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
import repast.simphony.util.collections.IndexedIterable;

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

	public boolean generated = true;
	public static boolean stop = false;

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

		// creates parking context to have access to only parking lot agents
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
	public void AddAgentsToEnvironent(ArrayList<ParkingLot> parkingLotAgents, ArrayList<Driver> driverAgents,
			Parameters params) {
		int nrParkingAgents = 8;// params.getInteger("parking_count");

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		schedule.schedule(ScheduleParameters.createRepeating(15, 15), this, "addDrivers");

		this.addParkingLots(nrParkingAgents, parkingLotAgents);
	}

	public void addParkingLots(int nrParkingAgents, ArrayList<ParkingLot> parkingLotAgents) {
		Point point;
		int type = 0;
		ArrayList<Coordinate> coords = new ArrayList<>();
		coords.add(new Coordinate(-1.5179463382681508, 53.830348355155316));
		coords.add(new Coordinate(-1.512861407674773, 53.8273409081473));
		coords.add(new Coordinate(-1.518021442707166, 53.829186442820216));
		coords.add(new Coordinate(-1.5192543771762335, 53.83033302471431));
		coords.add(new Coordinate(-1.5169192423121938, 53.833865282995106));
		coords.add(new Coordinate(-1.5170339492937859, 53.834459028019396));
		coords.add(new Coordinate(-1.5134111539939434, 53.83366239221787));
		coords.add(new Coordinate(-1.5097128267739806, 53.834141679352555));

		IndexedIterable<Junction> index = junctionContext.getObjects(Junction.class);
		for (int i = 0; i < index.size(); i++) {
			Coordinate jCoord = index.get(i).getCoords();
			if (coords.contains(jCoord)) {
				point = junctionGeography.getGeometry(index.get(i)).getCentroid();
				
				if(Initializer.experienceType.equals(ExperienceType.EXPERIENCE_1)) {
					addExperiment1Parkings(point, parkingLotAgents, type++);
				}else if(Initializer.experienceType.equals(ExperienceType.EXPERIENCE_2)){
					addExperiment2Parkings(point, parkingLotAgents, type++);
				}		
			}
		}
	}

	public void addDrivers() {
		if(stop) return;
		Random r = new Random();
		int nrDriverAgents;

		double offset = (r.nextInt(30) + 85) / 100;
		int currentDay = Initializer.manager.getDay();
		if (currentDay == 6 || currentDay == 7) {
			nrDriverAgents = (int) ((int) Initializer.manager.calculateNumberOfDriversWeekEndDays() * offset);
		} else {
			nrDriverAgents = (int) ((int) Initializer.manager.calculateNumberOfDriversWeekDays() * offset);
		}

		Junction junction;
		Road road;
		int type = 0;
		int maxTimeInPark = Manager.ticksPerHour*6+1;
		int maxTimeInParkMorningWorkers = Manager.ticksPerHour*2+1;

		ExploratoryDriver eDriver;
		RationalDriver rDriver;
		for (int i = 0; i < nrDriverAgents; i++) {
			type = r.nextInt(2);
			road = roadContext.getRandomObject();
			ArrayList<Junction> endpoints = road.getJunctions();
			Point initialPoint = junctionGeography.getGeometry(endpoints.get(0)).getCentroid();
			junction = junctionContext.getRandomObject();
			Point finalPoint = junctionGeography.getGeometry(junction).getCentroid();
			Coordinate initialCoordinate = new Coordinate(initialPoint.getX(), initialPoint.getY());
			Coordinate finalCoordinate = new Coordinate(finalPoint.getX(), finalPoint.getY());
			int durationOfStay;
			if (currentDay != 6 && currentDay != 7) {
				double ticksDay = Initializer.manager.getCurrentTickInDay();
				int minHour = Manager.ticksPerHour * 7;
				int maxHour = Manager.ticksPerHour * 9;
				if (ticksDay >= minHour && ticksDay <= maxHour) {
					durationOfStay = r.nextInt(maxTimeInParkMorningWorkers) + 18;
				} else {
					durationOfStay = r.nextInt(maxTimeInPark) + 6;
				}
			} else {
				durationOfStay = r.nextInt(maxTimeInPark) + 6;
			}
			double walkDistance = (r.nextInt(500) + 100); // [100,600[
			double defaultSatisfaction = r.nextInt(26)+40;

			if (type == 0) {
				eDriver = new ExploratoryDriver(initialCoordinate, finalCoordinate, durationOfStay, walkDistance,
						defaultSatisfaction);
				agentContext.add(eDriver);
				getAgentGeography().move(eDriver, initialPoint);
				Initializer.agentManager.acceptDriver(eDriver);
			} else {
				rDriver = new RationalDriver(initialCoordinate, finalCoordinate, durationOfStay, walkDistance,
						defaultSatisfaction);
				agentContext.add(rDriver);
				getAgentGeography().move(rDriver, initialPoint);
				Initializer.agentManager.acceptDriver(rDriver);
			}
		}
	}

	public void addExperiment1Parkings(Point point, ArrayList<ParkingLot> parkingLotAgents, int type) {
		Random r = new Random();
		int parkLotation;
		StaticParkingLot sPark;
		DynamicParkingLot dPark;
		
		parkLotation = r.nextInt(50) + 50;
		if (type < 4) {
			sPark = new StaticParkingLot(new Coordinate(point.getX(), point.getY()), parkLotation);
			agentContext.add(sPark);
			getAgentGeography().move(sPark, point);
			parkingLotContext.add(sPark);
			getParkingLotGeography().move(sPark, point);

			parkingLotAgents.add(sPark);
		} else {
			dPark = new DynamicParkingLot(new Coordinate(point.getX(), point.getY()), parkLotation);
			agentContext.add(dPark);
			getAgentGeography().move(dPark, point);
			parkingLotContext.add(dPark);
			getParkingLotGeography().move(dPark, point);

			parkingLotAgents.add(dPark);	
		}
		System.out.println("X: " + point.getX() + " ; Y: " + point.getY() + " " + parkLotation);
	}
	
	public void addExperiment2Parkings(Point point, ArrayList<ParkingLot> parkingLotAgents, int type) {
		Random r = new Random();
		int parkLotation;
		StaticParkingLot sPark;
		DynamicParkingLot dPark;
		CooperativeParkingLot cPark;
		
		parkLotation = r.nextInt(50) + 50;
		if (type < 2) {
			sPark = new StaticParkingLot(new Coordinate(point.getX(), point.getY()), parkLotation);
			agentContext.add(sPark);
			getAgentGeography().move(sPark, point);
			parkingLotContext.add(sPark);
			getParkingLotGeography().move(sPark, point);

			parkingLotAgents.add(sPark);
		} else if(type < 6) {
			cPark = new CooperativeParkingLot(new Coordinate(point.getX(), point.getY()), parkLotation);
			agentContext.add(cPark);
			getAgentGeography().move(cPark, point);
			parkingLotContext.add(cPark);
			getParkingLotGeography().move(cPark, point);

			parkingLotAgents.add(cPark);
		} else {
			dPark = new DynamicParkingLot(new Coordinate(point.getX(), point.getY()), parkLotation);
			agentContext.add(dPark);
			getAgentGeography().move(dPark, point);
			parkingLotContext.add(dPark);
			getParkingLotGeography().move(dPark, point);

			parkingLotAgents.add(dPark);
		}
		System.out.println("X: " + point.getX() + " ; Y: " + point.getY() + " " + parkLotation);
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
	 * 
	 * @param driver
	 */
	public static void removeAgent(Driver driver) {
		agentContext.remove(driver);
		agentGeography.move(driver, null);
	}

	/**
	 * Move an agent. This method is required -- rather than giving agents direct
	 * access to the agentGeography -- because when multiple threads are used they
	 * can interfere with each other and agents end up moving incorrectly.
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
	 * Move an agent by a vector. This method is required -- rather than giving
	 * agents direct access to the agentGeography -- because when multiple threads
	 * are used they can interfere with each other and agents end up moving
	 * incorrectly.
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
	 * Get the geometry of the given agent. This method is required -- rather than
	 * giving agents direct access to the agentGeography -- because when multiple
	 * threads are used they can interfere with each other and agents end up moving
	 * incorrectly.
	 */
	public static synchronized Geometry getAgentGeometry(Agent agent) {
		return agentGeography.getGeometry(agent);
	}

	/**
	 * Get the agent geometry
	 * 
	 * @return
	 */
	public static synchronized Geography<Agent> getAgentGeography() {
		return agentGeography;
	}

	/**
	 * Get the parking lot geometry
	 * 
	 * @return
	 */
	public static synchronized Geography<ParkingLot> getParkingLotGeography() {
		return parkingLotGeography;
	}

	/**
	 * Return the simulation context
	 * 
	 * @return
	 */
	public Context<Object> getContext() {
		return Simulation.context;
	}
}
