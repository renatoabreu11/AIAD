/*
�Copyright 2012 Nick Malleson
This file is part of RepastCity.

RepastCity is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

RepastCity is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with RepastCity.  If not, see <http://www.gnu.org/licenses/>.
*/

package environment;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.ArrayUtils;
import org.geotools.referencing.GeodeticCalculator;

import cern.colt.Arrays;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import repast.simphony.space.gis.Geography;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.graph.ShortestPath;
import agents.Agent;
import agents.ParkingLot;
import exceptions.RoutingException;
import parkingLot.GlobalVars;
import parkingLot.Simulation;

/**
 * Create routes around a GIS road network. The <code>setRoute</code> function actually finds the route and can be
 * overridden by subclasses to create different types of Route. See the method documentation for details of how routes
 * are calculated.
 * 
 * <p>
 * A "unit of travel" is the distance that an agent can cover in one iteration (one square on a grid environment or the
 * distance covered at walking speed in an iteration on a GIS environment). This will change depending on the type of
 * transport the agent is using. E.g. if they are in a car they will be able to travel faster, similarly if they are
 * travelling along a transort route they will cover more ground.
 * </p>
 * 
 * @author Nick Malleson
 */
public class Route {

	private static Logger LOGGER = Logger.getLogger(Route.class.getName());

	static {
		// Route.routeCache = new Hashtable<CachedRoute, CachedRoute>();
	}

	private Agent agent;
	private Coordinate destination;
	private Agent destinationBuilding;
	private int k=0;
	/*
	 * The route consists of a list of coordinates which describe how to get to the destination. Each coordinate might
	 * have an attached 'speed' which acts as a multiplier and is used to indicate whether or not the agent is
	 * travelling along a transport route (i.e. if a coordinate has an attached speed of '2' the agent will be able to
	 * get to the next coordinate twice as fast as they would do if they were walking). The current position incicate
	 * where in the lists of coords the agent is up to. Other attribute information about the route can be included as
	 * separate arrays with indices that match those of the 'route' array below.
	 */
	private int currentPosition;
	private List<Coordinate> routeX;
	private List<Double> routeSpeedsX;
	/*
	 * This maps route coordinates to their containing Road, used so that when travelling we know which road/community
	 * the agent is on. private
	 */
	private List<Road> roadsX;

	// Record which function has added each coord, useful for debugging
	private List<String> routeDescriptionX;

	/*
	 * Store a route once it has been created, might be used later (note that the same object acts as key and value).
	 */
	// TODO Re-think route caching, would be better to cache the whole Route object
	// private static volatile Map<CachedRoute, CachedRoute> routeCache;
	// /** Store a route distance once it has been created */
	// private static volatile Map<CachedRouteDistance, Double> routeDistanceCache;

	/*
	 * Keep a record of the last community and road passed so that the same buildings/communities aren't added to the
	 * cognitive map multiple times (the agent could spend a number of iterations on the same road or community).
	 */
	private Road previousRoad;
	private Area previousArea;

	/**
	 * Creates a new Route object.
	 * 
	 * @param burglar
	 *            The burglar which this Route will control.
	 * 
	 * @param destination
	 *            The agent's destination.
	 * 
	 * @param destinationBuilding
	 *            The (optional) building they're heading to.
	 * 
	 * @param type
	 *            The (optional) type of route, used by burglars who want to search.
	 */
	public Route(Agent agent, Coordinate destination, Agent destinationBuilding) {
		this.destination = destination;
		this.agent = agent;
		this.destinationBuilding = destinationBuilding;
	}

	/**
	 * Find a route from the origin to the destination. A route is a list of Coordinates which describe the route to a
	 * destination restricted to a road network. The algorithm consists of three major parts:
	 * <ol>
	 * <li>Find out if the agent is on a road already, if not then move to the nearest road segment</li>
	 * <li>Get from the current location (probably mid-point on a road) to the nearest junction</li>
	 * <li>Travel to the junction which is closest to our destination (using Dijkstra's shortest path)</li>
	 * <li>Get from the final junction to the road which is nearest to the destination
	 * <li>
	 * <li>Move from the road to the destination</li>
	 * </ol>
	 * 
	 * @throws Exception
	 */
	protected void setRoute() throws Exception {
		long time = System.nanoTime();
		// this.routeX = new ArrayList<Coordinate>();
		// this.roadsX = new ArrayList<Road>();
		// this.routeDescriptionX = new ArrayList<String>();
		// this.routeSpeedsX = new ArrayList<Double>();
		this.routeX = new Vector<Coordinate>();
		this.roadsX = new Vector<Road>();
		this.routeDescriptionX = new Vector<String>();
		this.routeSpeedsX = new Vector<Double>();

		if (atDestination()) {
			LOGGER.log(Level.WARNING, "Already at destination, cannot create a route for " + this.agent.toString());
			return;
		}

		Coordinate currentCoord = Simulation.getAgentGeometry(this.agent).getCoordinate();
		Coordinate destCoord = this.destination;

		try {

			/*
			 * Find the nearest junctions to our current position (road endpoints)
			 */

			// Start by Finding the road that this coordinate is on
			/*
			 * TODO EFFICIENCY: often the agent will be creating a new route from a building so will always find the
			 * same road, could use a cache. Even better, could implement a cache in FindNearestObject() method!
			 */
			Road currentRoad = Route.findNearestObject(currentCoord, Simulation.roadProjection, null,
					GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.LARGE);
			// Find which Junction is closest to us on the road.
			List<Junction> currentJunctions = currentRoad.getJunctions();

			/* Find the nearest Junctions to our destination (road endpoints) */

			// Find the road that this coordinate is on
			Road destRoad = Route.findNearestObject(destCoord, Simulation.roadProjection, null,
					GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.SMALL);
			// Find which Junction connected to the edge is closest to the coordinate.
			List<Junction> destJunctions = destRoad.getJunctions();
			/*
			 * Now have four possible routes (2 origin junctions, 2 destination junctions) need to pick which junctions
			 * form shortest route
			 */
			Junction[] routeEndpoints = new Junction[2];
			List<RepastEdge<Junction>> shortestPath = getShortestRoute(currentJunctions, destJunctions, routeEndpoints);
			// NetworkEdge<Junction> temp = (NetworkEdge<Junction>)
			// shortestPath.get(0);
			Junction currentJunction = routeEndpoints[0];
			Junction destJunction = routeEndpoints[1];

			/* Add the coordinates describing how to get to the nearest junction */
			List<Coordinate> tempCoordList = new Vector<Coordinate>();
			this.getCoordsAlongRoad(currentCoord, currentJunction.getCoords(), currentRoad, true, tempCoordList);
			addToRoute(tempCoordList, currentRoad, 1, "getCoordsAlongRoad (toJunction)");

			/*
			 * Add the coordinates and speeds etc which describe how to move along the chosen path
			 */
			this.getRouteBetweenJunctions(shortestPath, currentJunction);

			/*
			 * Add the coordinates describing how to get from the final junction to the destination.
			 */

			tempCoordList.clear();
			this.getCoordsAlongRoad(Simulation.junctionGeography.getGeometry(destJunction).getCoordinate(),
					destCoord, destRoad, false, tempCoordList);
			addToRoute(tempCoordList, destRoad, 1, "getCoordsAlongRoad (fromJunction)");

			// Check that a route has actually been created
			checkListSizes();

			// If the algorithm was better no coordinates would have been duplicated
			// removePairs();

			// Check lists are still the same size.
			checkListSizes();

		} catch (RoutingException e) {
			LOGGER.log(Level.SEVERE, "Route.setRoute(): Problem creating route for " + this.agent.toString()
					+ " going from " + currentCoord.toString() + " to " + this.destination.toString() + "("
					+ (this.destinationBuilding == null ? "" : this.destinationBuilding.toString())
					+ ") See earlier messages error messages for more info.");
			throw e;
		}

		LOGGER.log(Level.FINER, "Route Finished planning route for " + this.agent.toString() + "with "
				+ this.routeX.size() + " coords in " + (0.000001 * (System.nanoTime() - time)) + "ms.");

		// Finished, just check that the route arrays are all in sync
		assert this.roadsX.size() == this.routeX.size() && this.routeDescriptionX.size() == this.routeSpeedsX.size()
				&& this.roadsX.size() == this.routeDescriptionX.size();
	}

	private void checkListSizes() {
		assert this.roadsX.size() > 0 && this.roadsX.size() == this.routeX.size()
				&& this.routeDescriptionX.size() == this.routeSpeedsX.size()
				&& this.roadsX.size() == this.routeDescriptionX.size() : this.routeX.size() + "," + this.roadsX.size()
				+ "," + this.routeDescriptionX.size() + "," + this.routeSpeedsX.size();

	}

	/**
	 * Convenience function that can be used to add details to the route. This should be used rather than updating
	 * individual lists because it makes sure that all lists stay in sync
	 * 
	 * @param coord
	 *            The coordinate to add to the route
	 * @param road
	 *            The road that the coordinate is part of
	 * @param speed
	 *            The speed that the road can be travelled along
	 * @param description
	 *            A description of why the coordinate has been added
	 */
	private void addToRoute(Coordinate coord, Road road, double speed, String description) {
		this.routeX.add(coord);
		this.roadsX.add(road);
		this.routeSpeedsX.add(speed);
		this.routeDescriptionX.add(description);
	}

	/**
	 * A convenience for adding to the route that will add a number of coordinates with the same description, road and
	 * speed.
	 * 
	 * @param coord
	 *            A list of coordinates to add to the route
	 * @param road
	 *            The road that the coordinates are part of
	 * @param speed
	 *            The speed that the road can be travelled along
	 * @param description
	 *            A description of why the coordinates have been added
	 */
	private void addToRoute(List<Coordinate> coords, Road road, double speed, String description) {
		for (Coordinate c : coords) {
			this.routeX.add(c);
			this.roadsX.add(road);
			this.routeSpeedsX.add(speed);
			this.routeDescriptionX.add(description);
		}
	}

	/**
	 * Travel towards our destination, as far as we can go this turn.
	 * <p>
	 * Also adds houses to the agent's cognitive environment. This is done by saving each coordinate the person passes,
	 * creating a polygon with a radius given by the "cognitive_map_search_radius" and adding all houses which touch the
	 * polygon.
	 * <p>
	 * Note: the agent might move their position many times depending on how far they are allowed to move each turn,
	 * this requires many calls to geometry.move(). This function could be improved (quite easily) by working out where
	 * the agent's final destination will be, then calling move() just once.
	 * 
	 * @param housesPassed
	 *            If not null then the buildings which the agent passed during their travels this iteration will be
	 *            calculated and stored in this array. This can be useful if a agent needs to know which houses it has
	 *            just passed and, therefore, which are possible victims. This isn't done by default because it's quite
	 *            an expensive operation (lots of geographic tests which must be carried out in each iteration). If the
	 *            array is null then the houses passed are not calculated.
	 * @return null or the buildings passed during this iteration if housesPassed boolean is true
	 * @throws Exception
	 */
	public void travel() throws Exception {
		// Check that the route has been created
		if (this.routeX == null) {
			this.setRoute();
		}
		try {
			if (this.atDestination()) {
				return;
			}
			double time = System.nanoTime();

			// Store the roads the agent walks along (used to populate the awareness space)
			// List<Road> roadsPassed = new ArrayList<Road>();
			double distTravelled = 0; // The distance travelled so far
			Coordinate currentCoord = null; // Current location
			Coordinate target = null; // Target coordinate we're heading for (in route list)
			boolean travelledMaxDist = false; // True when travelled maximum distance this iteration
			double speed; // The speed to travel to next coord
			GeometryFactory geomFac = new GeometryFactory();
			currentCoord = Simulation.getAgentGeometry(this.agent).getCoordinate();

			while (!travelledMaxDist && !this.atDestination()) {
				target = this.routeX.get(this.currentPosition);
				speed = this.routeSpeedsX.get(this.currentPosition);
				double[] distAndAngle = new double[2];
				Route.distance(currentCoord, target, distAndAngle);
				// divide by speed because distance might effectively be shorter

				double distToTarget = distAndAngle[0] / speed;
				// If we can get all the way to the next coords on the route then just go there
				if (distTravelled + distToTarget < GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {

					distTravelled += distToTarget;
					currentCoord = target;

					// See if agent has reached the end of the route.
					if (this.currentPosition == (this.routeX.size() - 1)) {
						Simulation.moveAgent(this.agent, geomFac.createPoint(currentCoord));
						// Simulation.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
						break; // Break out of while loop, have reached end of route.
					}
					// Haven't reached end of route, increment the counter
					this.currentPosition++;
				} // if can get all way to next coord

				// Check if dist to next coordinate is exactly same as maximum
				// distance allowed to travel (unlikely but possible)
				else if (distTravelled + distToTarget == GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN) {
					travelledMaxDist = true;
					Simulation.moveAgent(agent, geomFac.createPoint(target));
					// Simulation.agentGeography.move(agent, geomFac.createPoint(target));
					this.currentPosition++;
					LOGGER.log(Level.WARNING, "Travel(): UNUSUAL CONDITION HAS OCCURED!");
				} else {
					// Otherwise move as far as we can towards the target along the road we're on.
					// Move along the vector the maximum distance we're allowed this turn (take into account relative
					// speed)
					double distToTravel = (GlobalVars.GEOGRAPHY_PARAMS.TRAVEL_PER_TURN - distTravelled) * speed;
					// Move the agent, first move them to the current coord (the first part of the while loop doesn't do
					// this for efficiency)
					// Simulation.agentGeography.move(this.agent, geomFac.createPoint(currentCoord));
					Simulation.moveAgent(this.agent, geomFac.createPoint(currentCoord));
					// Now move by vector towards target (calculated angle earlier).
					Simulation.moveAgentByVector(this.agent, distToTravel, distAndAngle[1]);
					// Simulation.agentGeography.moveByVector(this.agent, distToTravel, distAndAngle[1]);

					travelledMaxDist = true;
				} // else
			} // while

//			this.printRoute();

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Route.trave(): Caught error travelling for " + this.agent.toString()
					+ " going to " + "destination "
					+ (this.destinationBuilding == null ? "" : this.destinationBuilding.toString() + ")"));
			throw e;
		} // catch exception
	}

	/**
	 * Get the distance (on a network) between the origin and destination. Take into account the Burglar because they
	 * might be able to speed up the route by using different transport methods. Actually calculates the distance
	 * between the nearest Junctions between the source and destination. Note that the GRID environment doesn't have any
	 * transport routes in it so all distances will always be the same regardless of the agent.
	 * 
	 * @param agent
	 * @param destination
	 * @return
	 */
	public double getDistance(Agent theBurglar, Coordinate origin, Coordinate destination) {

		synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock) {
			GlobalVars.TRANSPORT_PARAMS.currentAgent = theBurglar;
			// Find the closest Junctions to the origin and destination
			double minOriginDist = Double.MAX_VALUE;
			double minDestDist = Double.MAX_VALUE;
			double dist;
			Junction closestOriginJunc = null;
			Junction closestDestJunc = null;
			DistanceOp distOp = null;
			GeometryFactory geomFac = new GeometryFactory();
			// TODO EFFICIENCY: here could iterate over near junctions instead of all?
			for (Junction j : Simulation.junctionContext.getObjects(Junction.class)) {
				// Check that the agent can actually get to the junction (if might be part of a transport route
				// that the agent doesn't have access to)
				boolean accessibleJunction = false;
				/*accessibleJunc: for (RepastEdge<Junction> e : Simulation.roadNetwork.getEdges(j)) {
					NetworkEdge<Junction> edge = (NetworkEdge<Junction>) e;
					for (String s : edge.getTypes()) {
						if (theBurglar.getTransportAvailable().contains(s)) {
							accessibleJunction = true;
							break accessibleJunc;
						}
					} // for types
				}// for edges
*/				if (!accessibleJunction) { // Agent can't get to the junction, ignore it
					continue;
				}
				Point juncPoint = geomFac.createPoint(j.getCoords());

				distOp = new DistanceOp(juncPoint, geomFac.createPoint(origin));
				dist = distOp.distance();
				if (dist < minOriginDist) {
					minOriginDist = dist;
					closestOriginJunc = j;
				}
				// Destination
				distOp = new DistanceOp(juncPoint, geomFac.createPoint(destination));
				dist = distOp.distance();
				if (dist < minDestDist) {
					minDestDist = dist;
					closestDestJunc = j;
				}
			}
			ShortestPath<Junction> p = new ShortestPath<Junction>(Simulation.roadNetwork);
			double theDist = p.getPathLength(closestOriginJunc, closestDestJunc);
			p.finalize();
			p = null;
			double finalDist = theDist + minOriginDist + minDestDist;
			return finalDist;
		} // synchronized

	}

	/**
	 * Finds the shortest route between multiple origin and destination junctions. Will return the shortest path and
	 * also, via two parameters, can return the origin and destination junctions which make up the shortest route.
	 * 
	 * @param currentJunctions
	 *            An array of origin junctions
	 * @param destJunctions
	 *            An array of destination junctions
	 * @param routeEndpoints
	 *            An array of size 2 which can be used to store the origin (index 0) and destination (index 1) Junctions
	 *            which form the endpoints of the shortest route.
	 * @return the shortest route between the origin and destination junctions
	 * @throws Exception
	 */
	private List<RepastEdge<Junction>> getShortestRoute(List<Junction> currentJunctions, List<Junction> destJunctions,
			Junction[] routeEndpoints) throws Exception {
		double time = System.nanoTime();
		synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock) {
			// This must be set so that NetworkEdge.getWeight() can adjust the weight depending on how this
			// particular agent is getting around the city
			GlobalVars.TRANSPORT_PARAMS.currentAgent = this.agent;
			double shortestPathLength = Double.MAX_VALUE;
			double pathLength = 0;
			ShortestPath<Junction> p;
			List<RepastEdge<Junction>> shortestPath = null;
			for (Junction o : currentJunctions) {
				for (Junction d : destJunctions) {
					if (o == null || d == null) {
						LOGGER.log(Level.WARNING, "Route.getShortestRoute() error: either the destination or origin "
								+ "junction is null. This can be caused by disconnected roads. It's probably OK"
								+ "to ignore this as a route should still be created anyway.");
					} else {
						p = new ShortestPath<Junction>(Simulation.roadNetwork);
						pathLength = p.getPathLength(o,d);
						if (pathLength < shortestPathLength) {
							shortestPathLength = pathLength;
							shortestPath = p.getPath(o,d);
							routeEndpoints[0] = o;
							routeEndpoints[1] = d;
						}
						// TODO See if the shortestpath bug has been fixed, would make this unnecessary
						p.finalize();
						p = null;
					} // if junc null
				} // for dest junctions
			} // for origin junctions
			if (shortestPath == null) {
				String debugString = "Route.getShortestRoute() could not find a route. Looking for the shortest route between :\n";
				for (Junction j : currentJunctions)
					debugString += "\t" + j.toString() + ", roads: " + j.getRoads().toString() + "\n";
				for (Junction j : destJunctions)
					debugString += "\t" + j.toString() + ", roads: " + j.getRoads().toString() + "\n";
				throw new RoutingException(debugString);
			}
			LOGGER.log(Level.FINER, "Route.getShortestRoute (" + (0.000001 * (System.nanoTime() - time))
					+ "ms) found shortest path " + "(length: " + shortestPathLength + ") from "
					+ routeEndpoints[0].toString() + " to " + routeEndpoints[1].toString());
			return shortestPath;
		} // synchronized
	}

	/**
	 * Calculates the coordinates required to move an agent from their current position to the destination along a given
	 * road. The algorithm to do this is as follows:
	 * <ol>
	 * <li>Starting from the destination coordinate, record each vertex and check inside the booundary of each line
	 * segment until the destination point is found.</li>
	 * <li>Return all but the last vertex, this is the route to the destination.</li>
	 * </ol>
	 * A boolean allows for two cases: heading towards a junction (the endpoint of the line) or heading away from the
	 * endpoint of the line (this function can't be used to go to two midpoints on a line)
	 * 
	 * @param currentCoord
	 * @param destinationCoord
	 * @param road
	 * @param toJunction
	 *            whether or not we're travelling towards or away from a Junction
	 * @param coordList
	 *            A list which will be populated with the coordinates that the agent should follow to move along the
	 *            road.
	 * @param roadList
	 *            A list of roads associated with each coordinate.
	 * @throws Exception
	 */
	private void getCoordsAlongRoad(Coordinate currentCoord, Coordinate destinationCoord, Road road,
			boolean toJunction, List<Coordinate> coordList) throws RoutingException {

		Route.checkNotNull(currentCoord, destinationCoord, road, coordList);

		double time = System.nanoTime();
		Coordinate[] roadCoords = Simulation.roadProjection.getGeometry(road).getCoordinates();

		// Check that the either the destination or current coordinate are actually part of the road
		boolean currentCorrect = false, destinationCorrect = false;
		for (int i = 0; i < roadCoords.length; i++) {
			if (toJunction && destinationCoord.equals(roadCoords[i])) {
				destinationCorrect = true;
				break;
			} else if (!toJunction && currentCoord.equals(roadCoords[i])) {
				currentCorrect = true;
				break;
			}
		} // for

		if (!(destinationCorrect || currentCorrect)) {
			String roadCoordsString = "";
			for (Coordinate c : roadCoords)
				roadCoordsString += c.toString() + " - ";
			throw new RoutingException("Neigher the origin or destination nor the current"
					+ "coordinate are part of the road '" + road.toString() + "' (person '" + this.agent.toString()
					+ "').\n" + "Road coords: " + roadCoordsString + "\n" + "\tOrigin: " + currentCoord.toString()
					+ "\n" + "\tDestination: " + destinationCoord.toString() + " ( "
					+ this.destinationBuilding.toString() + " )\n " + "Heading " + (toJunction ? "to" : "away from")
					+ " a junction, so " + (toJunction ? "destination" : "origin")
					+ " should be part of a road segment");
		}

		// Might need to reverse the order of the road coordinates
		if (toJunction && !destinationCoord.equals(roadCoords[roadCoords.length - 1])) {
			// If heading towards a junction, destination coordinate must be at end of road segment
			ArrayUtils.reverse(roadCoords);
		} else if (!toJunction && !currentCoord.equals(roadCoords[0])) {
			// If heading away form junction current coord must be at beginning of road segment
			ArrayUtils.reverse(roadCoords);
		}
		GeometryFactory geomFac = new GeometryFactory();
		Point destinationPointGeom = geomFac.createPoint(destinationCoord);
		Point currentPointGeom = geomFac.createPoint(currentCoord);
		// If still false at end then algorithm hasn't worked
		boolean foundAllCoords = false;
		search: for (int i = 0; i < roadCoords.length - 1; i++) {
			Coordinate[] segmentCoords = new Coordinate[] { roadCoords[i], roadCoords[i + 1] };
			// Draw a small buffer around the line segment and look for the coordinate within the buffer
			Geometry buffer = geomFac.createLineString(segmentCoords).buffer(GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE.SMALL.dist);
			if (!toJunction) {
				/* If heading away from a junction, keep adding road coords until we find the destination */
				coordList.add(roadCoords[i]);
				if (destinationPointGeom.within(buffer)) {
					coordList.add(destinationCoord);
					foundAllCoords = true;
					break search;
				}
			} else if (toJunction) {
				/*
				 * If heading towards a junction: find the curent coord, add it to the route, then add all the remaining
				 * coords which make up the road segment
				 */
				if (currentPointGeom.within(buffer)) {
					for (int j = i + 1; j < roadCoords.length; j++) {
						coordList.add(roadCoords[j]);
					}
					coordList.add(destinationCoord);
					foundAllCoords = true;
					break search;
				}
			}
		} // for
		if (foundAllCoords) {
			LOGGER.log(Level.FINER, "getCoordsAlongRoad (" + (0.000001 * (System.nanoTime() - time)) + "ms)");
			return;
		} else { // If we get here then the route hasn't been created
			// A load of debugging info
			String error = "Route: getCoordsAlongRoad: could not find destination coordinates "
					+ "along the road.\n\tHeading *" + (toJunction ? "towards" : "away from")
					+ "* a junction.\n\t Person: " + this.agent.toString() + ")\n\tDestination building: "
					+ destinationBuilding.toString() + "\n\tRoad causing problems: " + road.toString()
					+ "\n\tRoad vertex coordinates: " + Arrays.toString(roadCoords);
			throw new RoutingException(error);
			/*
			 * Hack: ignore the error, printing a message and just returning the origin destination and coordinates.
			 * This means agent will jump to/from the junction but I can't figure out why the fuck it occasionally
			 * doesn't work!! It's so rare that hopefully this isn't a problem.
			 */
			// TempLogger.err("Route: getCoordsAlongRoad: error... (not debugging).");
			// List<Coord> coords = new ArrayList<Coord>();
			// coords.add(currentCoord);
			// coords.add(destinationCoord);
			// for (Coord c : coords)
			// this.roads.put(c, road); // Remember the roads each coord is
			// // part of
			// return coords;

		}
	}

	private static void checkNotNull(Object... args) throws RoutingException {
		for (Object o : args) {
			if (o == null) {
				throw new RoutingException("An input argument is null");
			}
		}
		return;
	}

	/**
	 * Returns all the coordinates that describe how to travel along a path, restricted to road coordinates. In some
	 * cases the route wont have an associated road, this occurs if the route is part of a transport network. In this
	 * case just the origin and destination coordinates are added to the route.
	 * 
	 * @param shortestPath
	 * @param startingJunction
	 *            The junction the path starts from, this is required so that the algorithm knows which road coordinate
	 *            to add first (could be first or last depending on the order that the road coordinates are stored
	 *            internally).
	 * @return the coordinates as a mapping between the coord and its associated speed (i.e. how fast the agent can
	 *         travel to the next coord) which is dependent on the type of edge and the agent (e.g.
	 *         driving/walking/bus). LinkedHashMap is used to guarantee the insertion order of the coords is maintained.
	 * @throws RoutingException
	 */
	private void getRouteBetweenJunctions(List<RepastEdge<Junction>> shortestPath, Junction startingJunction)
			throws RoutingException {
		double time = System.nanoTime();
		if (shortestPath.size() < 1) {
			// This could happen if the agent's destination is on the same road
			// as the origin
			return;
		}
		// Lock the currentAgent so that NetworkEdge obejcts know what speed to use (depends on transport available to
		// the specific agent).
		synchronized (GlobalVars.TRANSPORT_PARAMS.currentBurglarLock) {
			GlobalVars.TRANSPORT_PARAMS.currentAgent = this.agent;

			// Iterate over all edges in the route adding coords and weights as appropriate
			NetworkEdge<Junction> e;
			Road r;
			// Use sourceFirst to represent whether or not the edge's source does actually represent the start of the
			// edge (agent could be going 'forwards' or 'backwards' over edge
			boolean sourceFirst;
			for (int i = 0; i < shortestPath.size(); i++) {
				e = (NetworkEdge<Junction>) shortestPath.get(i);
				if (i == 0) {
					// No coords in route yet, compare the source to the starting junction
					sourceFirst = (e.getSource().equals(startingJunction)) ? true : false;
				} else {
					// Otherwise compare the source to the last coord added to the list
					sourceFirst = (e.getSource().getCoords().equals(this.routeX.get(this.routeX.size() - 1))) ? true
							: false;
				}
				/*
				 * Now add the coordinates describing how to move along the road. If there is no road associated with
				 * the edge (i.e. it is a transport route) then just add the source/dest coords. Note that the shared
				 * coordinates between two edges will be added twice, these must be removed later
				 */
				r = e.getRoad();
				/*
				 * Get the speed that the agent will be able to travel along this edge (depends on the transport
				 * available to the agent and the edge). Some speeds will be < 1 if the agent shouldn't be using this
				 * edge but doesn't have any other way of getting to the destination. in these cases set speed to 1
				 * (equivalent to walking).
				 */
				double speed = e.getSpeed();
				if (speed < 1)
					speed = 1;

				if (r == null) { // No road associated with this edge (it is a
									// transport link) so just add source
					if (sourceFirst) {
						this.addToRoute(e.getSource().getCoords(), r, speed, "getRouteBetweenJunctions - no road");
						this.addToRoute(e.getTarget().getCoords(), r, -1, "getRouteBetweenJunctions - no road");
						// (Note speed = -1 used because we don't know the weight to the next
						// coordinate - this can be removed later)
					} else {
						this.addToRoute(e.getTarget().getCoords(), r, speed, "getRouteBetweenJunctions - no road");
						this.addToRoute(e.getSource().getCoords(), r, -1, "getRouteBetweenJunctions - no road");
					}
				} else {
					// This edge is a road, add all the coords which make up its geometry
					Coordinate[] roadCoords = Simulation.roadProjection.getGeometry(r).getCoordinates();
					if (roadCoords.length < 2)
						throw new RoutingException("Route.getRouteBetweenJunctions: for some reason road " + "'"
								+ r.toString() + "' doesn't have at least two coords as part of its geometry ("
								+ roadCoords.length + ")");
					// Make sure the coordinates of the road are added in the correct order
					if (!sourceFirst) {
						ArrayUtils.reverse(roadCoords);
					}
					// Add all the road geometry's coords
					for (int j = 0; j < roadCoords.length; j++) {
						this.addToRoute(roadCoords[j], r, speed, "getRouteBetweenJuctions - on road");
						// (Note that last coord will have wrong weight)
					} // for roadCoords.length
				} // if road!=null
			}
			// Check all lists are still the same size.
			assert this.roadsX.size() == this.routeX.size()
					&& this.routeDescriptionX.size() == this.routeSpeedsX.size()
					&& this.roadsX.size() == this.routeDescriptionX.size();

			// Check all lists are still the same size.
			assert this.roadsX.size() == this.routeX.size()
					&& this.routeDescriptionX.size() == this.routeSpeedsX.size()
					&& this.roadsX.size() == this.routeDescriptionX.size();

			// Finished!
			LOGGER.log(Level.FINER, "getRouteBetweenJunctions (" + (0.000001 * (System.nanoTime() - time)) + "ms");
			return;
		} // synchronized
	} // getRouteBetweenJunctions

	/**
	 * Determine whether or not the person associated with this Route is at their destination. Compares their current
	 * coordinates to the destination coordinates (must be an exact match).
	 * 
	 * @return True if the person is at their destination
	 */
	public boolean atDestination() {
		return Simulation.getAgentGeometry(this.agent).getCoordinate().equals(this.destination);
	}

	private void printRoute() {
		StringBuilder out = new StringBuilder();
		out.append("Printing route (" + this.agent.toString() + "). Current position in list is "
				+ this.currentPosition + " ('" + this.routeDescriptionX.get(this.currentPosition) + "')");
		for (int i = 0; i < this.routeX.size(); i++) {
			out.append("\t(" + this.agent.toString() + ") " + this.routeX.get(i).toString() + "\t"
					+ this.routeSpeedsX.get(i).toString() + "\t" + this.roadsX.get(i) + "\t"
					+ this.routeDescriptionX.get(i));
		}
		LOGGER.info(out.toString());
	}

	
	/**
	 * Find the nearest object in the given geography to the coordinate.
	 * 
	 * @param <T>
	 * @param x
	 *            The coordinate to search from
	 * @param geography
	 *            The given geography to look through
	 * @param closestPoints
	 *            An optional List that will be populated with the closest points to x (i.e. the results of
	 *            <code>distanceOp.closestPoints()</code>.
	 * @param searchDist
	 *            The maximum distance to search for objects in. Small distances are more efficient but larger ones are
	 *            less likely to find no objects.
	 * @return The nearest object.
	 * @throws RoutingException
	 *             If an object cannot be found.
	 */
	public static synchronized <T> T findNearestObject(Coordinate x, Geography<T> geography,
			List<Coordinate> closestPoints, GlobalVars.GEOGRAPHY_PARAMS.BUFFER_DISTANCE searchDist)
			throws RoutingException {
		if (x == null) {
			throw new RoutingException("The input coordinate is null, cannot find the nearest object");
		}

		T nearestObject = SpatialIndexManager.findNearestObject(geography, x, closestPoints, searchDist);
		if (nearestObject == null) {
			throw new RoutingException("Couldn't find an object close to these coordinates:\n\t" + x.toString());
		} else {
			return nearestObject;
		}
	}

	/**
	 * Returns the angle of the vector from p0 to p1 relative to the x axis
	 * <p>
	 * The angle will be between -Pi and Pi. I got this directly from the JUMP program source.
	 * 
	 * @return the angle (in radians) that p0p1 makes with the positive x-axis.
	 */
	public static synchronized double angle(Coordinate p0, Coordinate p1) {
		double dx = p1.x - p0.x;
		double dy = p1.y - p0.y;

		return Math.atan2(dy, dx);
	}

	/**
	 * The building which this Route is targeting
	 * 
	 * @return the destinationHouse
	 */
	public Agent getDestinationBuilding() {
		if (this.destinationBuilding == null) {
			LOGGER.log(Level.WARNING, "Route: getDestinationBuilding(), warning, no destination building has "
					+ "been set. This might be ok, the agent might be supposed to be heading to a coordinate "
					+ "not a particular building(?)");
			return null;
		}
		return destinationBuilding;
	}

	/**
	 * The coordinate the route is targeting
	 * 
	 * @return the destination
	 */
	public Coordinate getDestination() {
		return this.destination;
	}


	/**
	 * Calculate the distance (in meters) between two Coordinates, using the coordinate reference system that the
	 * roadGeography is using. For efficiency it can return the angle as well (in the range -0 to 2PI) if returnVals
	 * passed in as a double[2] (the distance is stored in index 0 and angle stored in index 1).
	 * 
	 * @param c1
	 * @param c2
	 * @param returnVals
	 *            Used to return both the distance and the angle between the two Coordinates. If null then the distance
	 *            is just returned, otherwise this array is populated with the distance at index 0 and the angle at
	 *            index 1.
	 * @return The distance between Coordinates c1 and c2.
	 */
	public static synchronized double distance(Coordinate c1, Coordinate c2, double[] returnVals) {
		// TODO check this now, might be different way of getting distance in new Simphony
		GeodeticCalculator calculator = new GeodeticCalculator(Simulation.roadProjection.getCRS());
		calculator.setStartingGeographicPoint(c1.x, c1.y);
		calculator.setDestinationGeographicPoint(c2.x, c2.y);
		double distance = calculator.getOrthodromicDistance();
		if (returnVals != null && returnVals.length == 2) {
			returnVals[0] = distance;
			double angle = Math.toRadians(calculator.getAzimuth()); // Angle in range -PI to PI
			// Need to transform azimuth (in range -180 -> 180 and where 0 points north)
			// to standard mathematical (range 0 -> 360 and 90 points north)
			if (angle > 0 && angle < 0.5 * Math.PI) { // NE Quadrant
				angle = 0.5 * Math.PI - angle;
			} else if (angle >= 0.5 * Math.PI) { // SE Quadrant
				angle = (-angle) + 2.5 * Math.PI;
			} else if (angle < 0 && angle > -0.5 * Math.PI) { // NW Quadrant
				angle = (-1 * angle) + 0.5 * Math.PI;
			} else { // SW Quadrant
				angle = -angle + 0.5 * Math.PI;
			}
			returnVals[1] = angle;
		}
		return distance;
	}

	/**
	 * Converts a distance lat/long distance (e.g. returned by DistanceOp) to meters. The calculation isn't very
	 * accurate because (probably) it assumes that the distance is between two points that lie exactly on a line of
	 * longitude (i.e. one is exactly due north of the other). For this reason the value shouldn't be used in any
	 * calculations which is why it's returned as a String.
	 * 
	 * @param dist
	 *            The distance (as returned by DistanceOp) to convert to meters
	 * @return The approximate distance in meters as a String (to discourage using this approximate value in
	 *         calculations).
	 * @throws Exception
	 * @see com.vividsolutions.jts.operation.distance.DistanceOp
	 */
	public static synchronized String distanceToMeters(double dist) throws Exception {
		// Works by creating two coords (close to a randomly chosen object) which are a certain distance apart
		// then using similar method as other distance() function
		GeodeticCalculator calculator = new GeodeticCalculator(Simulation.roadProjection.getCRS());
		Coordinate c1 = Simulation.junctionContext.getRandomObject().getCoords();
		calculator.setStartingGeographicPoint(c1.x, c1.y);
		calculator.setDestinationGeographicPoint(c1.x, c1.y + dist);
		return String.valueOf(calculator.getOrthodromicDistance());
	}

}
