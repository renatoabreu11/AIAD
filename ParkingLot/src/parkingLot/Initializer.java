package parkingLot;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import agents.Agent;
import agents.Driver;
import environment.FixedGeography;
import environment.GISFunctions;
import environment.Junction;
import environment.NetworkEdgeCreator;
import environment.Road;
import environment.contexts.JunctionContext;
import environment.contexts.RoadContext;
import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.gis.ShapefileLoader;
import repast.simphony.space.gis.SimpleAdder;
import repast.simphony.space.graph.Network;

public class Initializer implements ContextBuilder<Object> {
	
	public static Context<Agent> agentContext;

	public static Context<Road> roadContext;
	public static Geography<Road> roadProjection;
	
	public static Context<Junction> junctionContext;
	public static Geography<Junction> junctionGeography;
	
	public static Network<Junction> roadNetwork;
	
	@Override
	public Context build(Context<Object> context) {
		context.setId("ParkingLot");

		
		// Create the Roads - context and geography
		roadContext = new RoadContext();
		roadProjection = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
				"RoadGeography", roadContext,
				new GeographyParameters<Road>(new SimpleAdder<Road>()));

		try {
			GISFunctions.readShapefile(Road.class, "espinho.shp", roadProjection, roadContext);
			context.addSubContext(roadContext);

			
			junctionContext = new JunctionContext();
			context.addSubContext(junctionContext);
			junctionGeography = GeographyFactoryFinder.createGeographyFactory(null).createGeography(
					"JunctionGeography", junctionContext,
					new GeographyParameters<Junction>(new SimpleAdder<Junction>()));

			NetworkBuilder<Junction> builder = new NetworkBuilder<Junction>(GlobalVars.CONTEXT_NAMES.ROAD_NETWORK,
					junctionContext, false);
			builder.setEdgeCreator(new NetworkEdgeCreator<Junction>());
			roadNetwork = builder.buildNetwork();
			GISFunctions.buildGISRoadNetwork(roadProjection, junctionContext, junctionGeography, roadNetwork);
			
		} catch (MalformedURLException | FileNotFoundException e) {
			e.printStackTrace();
		}

		
		
		if (RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(20);
		}

		return context;
		
		
	}
	
	public static <T extends FixedGeography> void readShapefile(Class<T> cl, String shapefileLocation,
			Geography<T> geog, Context<T> context) throws MalformedURLException, FileNotFoundException {
		File shapefile = null;
		ShapefileLoader<T> loader = null;
		shapefile = new File(shapefileLocation);
		if (!shapefile.exists()) {
			throw new FileNotFoundException("Could not find the given shapefile: " + shapefile.getAbsolutePath());
		}
		loader = new ShapefileLoader<T>(cl, shapefile.toURI().toURL(), geog, context);
		while (loader.hasNext()) {
			loader.next();
		}
		for (T obj : context.getObjects(cl)) {
			obj.setCoords(geog.getGeometry(obj).getCentroid().getCoordinate());
		}
	}

}
