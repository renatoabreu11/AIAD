package parkingLot;

import java.lang.*;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;

import agents.ParkingLot;

public class Utils {

	private static Utils instance = null;
	public static ArrayList<ParkingLot> parks = new ArrayList<>();

	protected Utils() {
		// Exists only to defeat instantiation.
	}

	public static Utils getInstance() {
		if (instance == null) {
			instance = new Utils();
		}
		return instance;
	}
}
