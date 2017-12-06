package behaviours;

import agents.ParkingLot;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.behaviours.CyclicBehaviour;

public class RequestExitServer extends CyclicBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1228537486635629327L;

	/**
	 * Cyclic behaviour that waits from requests to exit the park by driver agents
	 */
	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			((ParkingLot) myAgent).removeDriver(msg.getSender().getName());
		}
		else {
			block();
		}
	}
} 