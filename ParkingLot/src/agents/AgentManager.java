package agents;

import java.util.logging.Logger;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import parkingLot.Initializer;
import sajas.domain.DFService;
import sajas.wrapper.ContainerController;

public class AgentManager extends Agent{
	private static Logger LOGGER = Logger.getLogger(AgentManager.class.getName());
	private static ContainerController mainContainer;
	
	public AgentManager(ContainerController mainContainer) {
		super("AgentManager", Type.AGENT_MANAGER);
		this.mainContainer = mainContainer;
	}
	
	@Override
	protected void setup() {
		LOGGER.info("AgentManager " + getAID().getName()  + " is ready!");
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getName());
		sd.setType("AgentManager");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		LOGGER.info("AgentManager " + getAID().getName()  + " terminating");
	}
}
