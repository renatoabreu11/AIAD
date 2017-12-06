package agents;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import environment.Junction;
import environment.Road;
import environment.Route;
import parkingLot.Initializer;
import parkingLot.Utils;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.graph.RepastEdge;

public class Driver extends IAgent {
	private static Logger LOGGER = Logger.getLogger(Driver.class.getName());

	public int totalTicksParked = 0;

	public int parkingDuration = 10; // definir valor default futuramente
	public  double maxDist = 100.0; // definir valor default futuramente

	public boolean alive = true;

	public Coordinate destination;
	public Coordinate currentPosition;
	public ArrayList<Junction> junctionsToPass;
	// public int currentJunction;
	public Road currentRoad;

	Route route;
	private boolean goingHome;

	public Driver() {

	}

	public Driver(Coordinate currentPosition, Coordinate destination, ArrayList<Junction> junctions, Road firstRoad) {
		this.currentPosition = currentPosition;
		this.junctionsToPass = junctions;
		this.currentRoad = firstRoad;
		this.destination = destination;
		
		//this.getPossibleParks();
		
		for(int i=0; i<Initializer.agentContext.getObjects(ParkingLot.class).size();i++) {
			
		}
		
		IAgent b = Initializer.agentContext.getRandomObjects(ParkingLot.class, 1).iterator().next();
		this.route = new Route(this, Initializer.getAgentGeography().getGeometry(b).getCoordinate(), b);
		LOGGER.log(Level.FINE, this.toString() + " created new route to " + b.toString());
	}
	
	private void getPossibleParks() {
		Coordinate tmp = new Coordinate();
		double[] distAndAng = new double[2];
		for(int i = 0;i < Utils.getInstance().parks.size(); i++) {
			tmp = Utils.getInstance().parks.get(i).getCurrentPosition();
			Route.distance(this.destination, tmp, distAndAng);
			System.out.println(tmp.toString()+" , "+distAndAng[0]);
		}
		
		System.out.println("Destination "+this.destination.toString());
	}

	public Coordinate getDestination() {
		return destination;
	}
	
	public Coordinate getCurrentPosition() {
		return currentPosition;
	}

	public ArrayList<Junction> getJunctionsToPass() {
		return junctionsToPass;
	}

	public Road getCurrentRoad() {
		return currentRoad;
	}

	public boolean getAlive() {
		return alive;
	}

	public void update() {
		if(this.alive) {
			IAgent.updateTick();
			if (!this.route.atDestination()) {
				try {
					this.route.travel();
				} catch (Exception e) {
					e.printStackTrace();
				}
				LOGGER.log(Level.FINE,
						this.toString() + " travelling to " + this.route.getDestinationBuilding().toString());
			} else {
				// Chegou ao destino
				LOGGER.log(Level.FINE, this.toString() + " reached final destination: " + this.route.getDestinationBuilding().toString());
				this.updateParkingTime();
			}
		}
		else {
			System.out.println("Sair do programa");
		}
	}
	
	public void updateParkingTime() {
		System.out.println("Ticks parked: "+this.totalTicksParked);
		if(this.totalTicksParked < this.parkingDuration) {
			this.totalTicksParked++;
		}
		else {
			this.alive = false;
		}
	}
}
