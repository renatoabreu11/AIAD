package behaviours;

import agents.ParkingLot;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.behaviours.CyclicBehaviour;

public class RequestEntryServer extends CyclicBehaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1254981594111718979L;
	
	/**
	 * RequestEntryServer behaviour. 
	 * Cyclic behaviour active in each park that accepts request from drivers and send them the price per stay
	 */
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchConversationId("park-entry");
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null && msg.getPerformative() == ACLMessage.CFP) {
			String durationOfStay = msg.getContent();
			ACLMessage reply = msg.createReply();

			double price = ((ParkingLot) myAgent).getFinalPrice(durationOfStay);
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent(String.valueOf(price));
			((ParkingLot) myAgent).logMessage("Request entry served\n" + reply);
			myAgent.send(reply);
		}
		else {
			block();
		}
	}
}
