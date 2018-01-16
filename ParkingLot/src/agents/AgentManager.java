package agents;

import java.util.ArrayList;
import java.util.logging.Logger;

import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.StaleProxyException;
import repast.simphony.parameter.Parameters;
import sajas.domain.DFService;
import sajas.wrapper.ContainerController;

public class AgentManager extends Agent{
	private static Logger LOGGER = Logger.getLogger(AgentManager.class.getName());
	
	private static ContainerController mainContainer;
	public ArrayList<ParkingLot> parkingAgents;
	public ArrayList<Driver> driverAgents;
	
	public AgentManager(ContainerController mainContainer) {
		super("AgentManager", Type.AGENT_MANAGER);
		AgentManager.mainContainer = mainContainer;
		parkingAgents = new ArrayList<>();
		driverAgents = new ArrayList<>();
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
	
	public void initAgents(Parameters params) {
		int nrDriverAgents = params.getInteger("driver_count");
		int nrParkingAgents = params.getInteger("parking_count");
		
		for(int i = 0; i < nrDriverAgents; i++) {
			Driver d = new Driver();
			driverAgents.add(d);
		}
		
		for(int i = 0; i < nrParkingAgents; i++) {
			ParkingLot pl = new ParkingLot();
			parkingAgents.add(pl);
		}
	}

	public void startAgents() {
		for(int i = 0; i < driverAgents.size(); i++) {
			Driver d = new Driver();
			try {
				mainContainer.acceptNewAgent(driverAgents.get(i).getName(), driverAgents.get(i)).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < parkingAgents.size(); i++) {
			try {
				mainContainer.acceptNewAgent(parkingAgents.get(i).getName(), parkingAgents.get(i)).start();
			} catch (StaleProxyException e) {
				e.printStackTrace();
			}
		}
	}
}
