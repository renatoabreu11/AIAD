package behaviours;

import agents.ParkingLot;
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
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			String durationOfStay = msg.getContent();
			ACLMessage reply = msg.createReply();

			boolean accepted = ((ParkingLot) myAgent).acceptDriver(durationOfStay, msg.getSender().getName());
			if(accepted) {
				reply.setPerformative(ACLMessage.INFORM);
				System.out.println("Driver " + reply.getSender().getName() + " successfully parked at " + myAgent.getName());
				myAgent.send(reply);	
			} else {
				reply.setPerformative(ACLMessage.FAILURE);
				reply.setContent("no-spots-available");
			}
		}
		else {
			block();
		}
	}
} 