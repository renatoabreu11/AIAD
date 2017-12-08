package behaviours;

import agents.Driver;
import agents.ParkingLot;
import jade.lang.acl.ACLMessage;
import sajas.core.AID;
import sajas.core.Agent;
import sajas.core.behaviours.WakerBehaviour;

public class RequestExitPerformer extends WakerBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1228537486635629327L;
	private AID currParkingAgent;

	/**
	 * Behaviour that executes only once, after a given timeout. In this case, sends a message to the park, informing that it will exit
	 * @param a
	 * @param period
	 */
	public RequestExitPerformer(Agent a, long timeout, AID currParkingAgent) {
		super(a, timeout);
		this.currParkingAgent = currParkingAgent;
	}
	
	@Override
	protected void onWake() {
		ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
		cfp.addReceiver(currParkingAgent);
		cfp.setConversationId("park-exit");
		cfp.setReplyWith("cfp"+System.currentTimeMillis());
		myAgent.send(cfp);
		((Driver) myAgent).logMessage("Request exit performed\n" + cfp);
		myAgent.doDelete();
	}
} 