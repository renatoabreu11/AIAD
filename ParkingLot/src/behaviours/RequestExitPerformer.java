package behaviours;

import agents.driver.Driver;
import jade.lang.acl.ACLMessage;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.OneShotBehaviour;

public class RequestExitPerformer extends OneShotBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1228537486635629327L;
	private AID currParkingAgent;

	/**
	 * Behaviour that executes only once. In this case, sends a message to the park, informing that it will exit
	 * @param a
	 * @param period
	 */
	public RequestExitPerformer(Agent a, AID currParkingAgent) {
		super(a);
		this.currParkingAgent = currParkingAgent;
	}
	
	@Override
	public void action() {
		ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
		cfp.addReceiver(currParkingAgent);
		cfp.setConversationId("park-exit");
		cfp.setReplyWith("cfp"+System.currentTimeMillis());
		myAgent.send(cfp);
		((Driver) myAgent).logMessage("Request exit performed\n" + cfp);
		((Driver) myAgent).setAlive(false);
	}
} 