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
	public static enum ExperienceType {
		EXPERIENCE_1,
		EXPERIENCE_2
	}
	
	private static Logger LOGGER = Logger.getLogger(Initializer.class.getName());
	private static long speedTimer = -1;
	
	public static AgentManager agentManager;
	public static Manager manager;
	private static ContainerController mainContainer;
	private Simulation simulation;
	
	public static ExperienceType experienceType = ExperienceType.EXPERIENCE_1;

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
		simulation.AddAgentsToEnvironent(agentManager.parkingAgents, agentManager.driverAgents,params);
		agentManager.startAgents();
	}
	
	@Override
	public Context<?> build(Context<Object> context) {
		context.setId("ParkingLotSimulation");
		simulation = new Simulation(context);
		simulation.addEnvironment();
		manager = new Manager();
		simulation.addAgentsToContext(manager);
		
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