package parkingLot;

public class Utils {

	private static Utils instance = null;

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
