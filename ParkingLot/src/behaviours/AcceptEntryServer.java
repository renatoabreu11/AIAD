package behaviours;

import agents.parkingLot.ParkingLot;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.behaviours.CyclicBehaviour;

public class AcceptEntryServer extends CyclicBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1228537486635629327L;

	/**
	 * AcceptEntryServer behaviour. 
	 * Cyclic behaviour active in each park that that allows or refuses the entry of drivers to the respective park
	 */
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchConversationId("park-accept");
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null && msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			String durationOfStay = msg.getContent();
			ACLMessage reply = msg.createReply();
		
			boolean accepted = ((ParkingLot) myAgent).acceptDriver(durationOfStay, msg.getSender().getName());
			if(accepted) {
				reply.setPerformative(ACLMessage.INFORM);
				myAgent.send(reply);
			} else {
				reply.setPerformative(ACLMessage.FAILURE);
				reply.setContent("No spots available.");
				myAgent.send(reply);
			}
			((ParkingLot) myAgent).logMessage("Accept entry served\n" + reply);
		}
		else {
			block();
		}
	}
} 