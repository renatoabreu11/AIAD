package agents;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;

import environment.Junction;
import environment.Road;
import environment.Route;
import parkingLot.Initializer;
import repast.simphony.util.collections.IndexedIterable;

public class Driver extends Agent {
	private static Logger LOGGER = Logger.getLogger(Driver.class.getName());

	private int totalTicksParked = 0;

	private int parkingDuration = 10; // definir valor default futuramente
	private  double maxDist = 400.0; // definir valor default futuramente

	private boolean alive = true;
	private boolean inPark = false;
	
	private ArrayList<ParkingLot> parksInRange = new ArrayList<>();
	private ParkingLot parkingLotDestiny = null;

	public Coordinate destination;
	public Coordinate currentPosition;
	//public ArrayList<Junction> junctionsToPass;
	// public int currentJunction;
	//public Road currentRoad;

	Route route;

	public Driver(Coordinate currentPosition, Coordinate destination) {
		super("driver");
		this.currentPosition = currentPosition;
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
			tmp = parks.get(i).getPosition();
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
			this.parkingLotDestiny = this.parksInRange.get(0);
			this.route = new Route(this, Initializer.getAgentGeography().getGeometry(parkingLotDestiny).getCoordinate(), parkingLotDestiny);
			LOGGER.log(Level.FINE, this.toString() + " created new route to " + parkingLotDestiny.toString());
		}
	}

	public Coordinate getDestination() {
		return destination;
	}
	
	public Coordinate getCurrentPosition() {
		return currentPosition;
	}
	
	public boolean getAlive() {
		return alive;
	}

	public void update() {
		if(this.alive) {
			Agent.updateTick();
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
				if(!this.inPark) {
					this.inPark = true;
					System.out.println("CHEGOU");
					this.parkingLotDestiny.acceptDriver(this); // fazer a chamada para adicionar
				}
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
			this.parkingLotDestiny.removeDriver(this); // fazer a chamada para remover
		}
	}
}
