package agents;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import environment.Junction;
import environment.Road;
import environment.Route;
import parkingLot.Initializer;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.RepastEdge;

public class Driver extends IAgent {
	private static Logger LOGGER = Logger.getLogger(Driver.class.getName());
	
	public static int totalTicksParked = 0;
	public static int totalTicksAlive = 0;
	
	public static int parkingDuration = 0; // definir valor default futuramente
	public static int maxDist = 0; // definir valor default futuramente
	
	
	public boolean alive = true;
	
	public Coordinate destination;
	public List<RepastEdge<Junction>> junctionsToPass;
	public int currentJunction;
	public Road currentRoad;

	Route route;
	private boolean goingHome;
	
	public Driver() {
		
	}
	
	public Driver(Coordinate destination,List<RepastEdge<Junction>> junctionsToPass,Road firstRoad) {
		this.destination=destination;
		this.junctionsToPass=junctionsToPass;
		this.currentRoad=firstRoad;
		this.currentJunction=this.junctionsToPass.get(0).getSource().getId(); //n�o tenho a certeza, perguntar ao William
	}
	
	public Coordinate getDestination() {
		return destination;
	}
	
	public List<RepastEdge<Junction>> getJunctionsToPass(){
		return junctionsToPass;
	}
	
	public int getCurrentJunction() {
		return currentJunction;
	}
	
	public Road getCurrentRoad() {
		return currentRoad;
	}
	
	public boolean getAlive() {
		return alive;
	}
	
	
	public void update() {

		if (this.route == null) {
			this.goingHome = false; // Must be leaving home
			// Choose a new building to go to
			IAgent b = Initializer.agentContext.getRandomObjects(ParkingLot.class, 1).iterator().next();

			this.route = new Route(this, Initializer.getAgentGeography().getGeometry(b).getCoordinate(), b);
			LOGGER.log(Level.FINE, this.toString() + " created new route to " + b.toString());
		}
		if (!this.route.atDestination()) {
			try {
				this.route.travel();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LOGGER.log(Level.FINE, this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
		} else {
			// Have reached destination, now either go home or onto another building
			LOGGER.log(Level.FINE, this.toString() + " reached final destination: " + this.route.getDestinationBuilding().toString());
			this.route = null;

		}
	}
}
