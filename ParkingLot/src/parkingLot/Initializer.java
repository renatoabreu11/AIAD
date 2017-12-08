package parkingLot;

import java.util.logging.Logger;

import agents.AgentManager;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import sajas.sim.repasts.RepastSLauncher;
import sajas.wrapper.ContainerController;

public class Initializer extends RepastSLauncher{
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	private static long speedTimer = -1;
	
	private static ContainerController mainContainer;
	public static AgentManager agentManager;
	private Simulation simulation;

	@Override
	public String getName() {
		return "ParkingLotSimulation";
	}

	@Override
	protected void launchJADE() {
		LOGGER.info("Launching JADE wrapper");
		sajas.core.Runtime rt = sajas.core.Runtime.instance();
		
		Profile profile = new ProfileImpl();
		mainContainer = rt.createMainContainer(profile);
		
		agentManager = new AgentManager(mainContainer);
		
		try {
			mainContainer.acceptNewAgent(agentManager.getName(), agentManager).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		agentManager.initAgents(params);
		simulation.AddAgentsToEnvironent(agentManager.parkingAgents, agentManager.driverAgents);
		agentManager.startAgents();
		
	}
	
	@Override
	public Context<?> build(Context<Object> context) {
		context.setId("ParkingLotSimulation");
		simulation = new Simulation(context);
		simulation.addEnvironment();
		simulation.addAgentsToContext();
		
		return super.build(simulation.getContext());
	}
	
	/**
	 * Print information about each iteration
	 */
	public void printTicks() {
		/*LOGGER.info("Iterations: " + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()+
				". Speed: "+((double)(System.currentTimeMillis()-speedTimer)/1000.0)+
				"sec/ticks.");*/
		speedTimer = System.currentTimeMillis();
	}
}