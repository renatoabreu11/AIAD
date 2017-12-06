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
import repast.simphony.util.collections.IndexedIterable;

public class Driver extends IAgent {
	private static Logger LOGGER = Logger.getLogger(Driver.class.getName());

	private int totalTicksParked = 0;

	private int parkingDuration = 10; // definir valor default futuramente
	private  double maxDist = 200.0; // definir valor default futuramente

	private boolean alive = true;
	
	private ArrayList<ParkingLot> parksInRange = new ArrayList<>();

	public Coordinate destination;
	public Coordinate currentPosition;
	public ArrayList<Junction> junctionsToPass;
	// public int currentJunction;
	public Road currentRoad;

	Route route;
	
	public Driver() {

	}

	public Driver(Coordinate currentPosition, Coordinate destination, ArrayList<Junction> junctions, Road firstRoad) {
		this.currentPosition = currentPosition;
		this.junctionsToPass = junctions;
		this.currentRoad = firstRoad;
		this.destination = destination;
		
		System.out.println("Destination "+this.destination.toString());
		
		this.getPossibleParks();
		this.pickParkToGo();
	}
	
	private void getPossibleParks() {
		Coordinate tmp = new Coordinate();
		double[] distAndAng = new double[2];
		IndexedIterable<ParkingLot> parks = Initializer.parkingLotContext.getObjects(ParkingLot.class);
		
		for(int i = 0;i<parks.size();i++) {
			tmp = parks.get(i).getCurrentPosition();
			Route.distance(this.destination, tmp, distAndAng);
			System.out.println("I"+i+": "+distAndAng[0]+" ; "+distAndAng[1]);
			if(distAndAng[0] < this.maxDist) {
				parksInRange.add(parks.get(i));
			}
		}
	}
	
	public void pickParkToGo() {
		if(this.parksInRange.size() == 0) {
			this.alive = false;
		}
		else {
			ParkingLot tmpParking = this.parksInRange.get(0);
			this.route = new Route(this, Initializer.getAgentGeography().getGeometry(tmpParking).getCoordinate(), tmpParking);
			LOGGER.log(Level.FINE, this.toString() + " created new route to " + tmpParking.toString());
		}
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
