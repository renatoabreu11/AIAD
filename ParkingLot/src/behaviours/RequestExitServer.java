package behaviours;

import agents.parkingLot.ParkingLot;
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
		MessageTemplate mt = MessageTemplate.MatchConversationId("park-exit");
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
			((ParkingLot) myAgent).removeDriver(msg.getSender().getName());
		}
		else {
			block();
		}
	}
} 