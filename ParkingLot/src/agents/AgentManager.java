package agents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;
import agents.driver.Driver;
import agents.parkingLot.ParkingLot;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.StaleProxyException;
import parkingLot.Initializer;
import parkingLot.Manager;
import repast.simphony.engine.schedule.ScheduledMethod;
import sajas.domain.DFService;
import sajas.wrapper.ContainerController;
import behaviours.WeeklyUpdatePerformer;;

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
	
	@ScheduledMethod(start = 1, interval = 1)
	public void update() {
		if(Initializer.manager.getCurrentTickInWeek() == (Manager.ticksPerWeek - Manager.ticksPerHour)) { // One hour before the week ends, it stores the weekly info
			for(ParkingLot pl : parkingAgents) {
				pl.addBehaviour(new WeeklyUpdatePerformer());
			}
		}
	};
	
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
	
	public void startAgents() {
		for(int i = 0; i < driverAgents.size(); i++) {
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
	
	public void acceptDriver(Driver driver) {
		this.driverAgents.add(driver);
		try {
			mainContainer.acceptNewAgent(driver.getName(), driver).start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void removeAgent(String AID) {
		for (Iterator<Driver> iterator = driverAgents.iterator(); iterator.hasNext();) {
		    Driver d = iterator.next();
		    if (AID.equals(d.getAID().toString())) {
		        iterator.remove();
		        return;
		    }
		}
	}
	
	public synchronized ParkingLot getAgent(String AID) {
		for (Iterator<ParkingLot> iterator = parkingAgents.iterator(); iterator.hasNext();) {
		    ParkingLot d = iterator.next();
		    if (AID.equals(d.getAID().toString())) {
		        return d;
		    }
		}
		return null;
	}
	
	public synchronized ArrayList<ParkingLot> getParkingAgents() {
		return this.parkingAgents;
	}
}
