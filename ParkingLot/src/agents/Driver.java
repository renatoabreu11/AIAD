package agents;

import java.util.List;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import environment.Junction;
import environment.Road;
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
	
	public Driver() {
		
	}
	
	public Driver(Coordinate destination,List<RepastEdge<Junction>> junctionsToPass,Road firstRoad) {
		this.destination=destination;
		this.junctionsToPass=junctionsToPass;
		this.currentRoad=firstRoad;
		this.currentJunction=this.junctionsToPass.get(0).getSource().getId(); //não tenho a certeza, perguntar ao William
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
	
	
	/*@ScheduledMethod(start = 1, interval = 1)
	public void step() {

		Coordinate current = Initializer.agentGeography.getGeometry(this).getCoordinate(); 
		RepastEdge<Junction> iteration = junctionsToPass.get(currentJunction);
		Junction previous = iteration.getSource();
		Coordinate prevCoord = Initializer.junctionGeography.getGeometry(previous).getCoordinate();
		Coordinate[] c = Initializer.roadProjection.getGeometry(currentRoad).getCoordinates();
		for(int i = 0; i < c.length - 1; i++) {
			if(current.equals(c[i])) {
				Coordinate nextCoord;
				if(prevCoord == c[0]) {
					nextCoord = c[i+1];
				} else {
					nextCoord = c[i-1];
				}
				GeometryFactory geoFactory = new GeometryFactory();
				LOGGER.info(nextCoord.toString());
				Initializer.agentGeography.move(this, geoFactory.createPoint(nextCoord));
			}
		}
	}*/
}
