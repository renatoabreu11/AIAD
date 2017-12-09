package behaviours;

import agents.driver.Driver;
import agents.parkingLot.ParkingLot;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.AID;
import sajas.core.behaviours.Behaviour;

public class RequestEntryPerformer extends Behaviour {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8161159508758712663L;
	private AID parkingAgent;
	private String durationOfStay;
	private double price;
	private double driverUtility;
	private MessageTemplate mt; // The template to receive replies
	private int step = 0;
	
	public RequestEntryPerformer(AID parkingLotAID, int duratioOfStay) {
		this.durationOfStay = String.valueOf(duratioOfStay);
		this.parkingAgent = parkingLotAID;
	}

	public void action() {
		switch (step) {
		case 0:
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.addReceiver(parkingAgent);
			
			cfp.setContent(durationOfStay);
			cfp.setConversationId("park-entry");
			cfp.setReplyWith("cfp"+System.currentTimeMillis());
			myAgent.send(cfp);
			((Driver) myAgent).logMessage("Request entry performed\n" + cfp);
			
			// Prepare the template to get proposals
			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("park-entry"),
					MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			step = 1;
			break;
		case 1:
			// Receive all proposals/refusals from seller agents
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				// Reply received
				if (reply.getPerformative() == ACLMessage.PROPOSE) {
					price = Double.parseDouble(reply.getContent());
					
					driverUtility = ((Driver) myAgent).getUtility(price);
					step = 2; 
				}
			}
			else {
				block();
			}
			break;
		case 2:
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.addReceiver(parkingAgent);
			order.setContent(durationOfStay);
			order.setConversationId("park-entry");
			order.setReplyWith("entry "+System.currentTimeMillis());
			myAgent.send(order);
			
			((Driver) myAgent).logMessage("Accept entry performed\n" + order);

			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("park-entry"),
					MessageTemplate.MatchInReplyTo(order.getReplyWith()));
			step = 3;
			break;
		case 3:      
			reply = myAgent.receive(mt);
			if (reply != null) {
				if (reply.getPerformative() == ACLMessage.INFORM) {
					((Driver) myAgent).logMessage("Driver " + myAgent.getName() + " successfully parked at " + reply.getSender().getName() + 
							"\nPrice = " + price + "; Parking duration: " + durationOfStay);
					((Driver) myAgent).setParked(true);
				}
				else {
					((Driver) myAgent).logMessage("Park entry failed: park at maximum capacity");
				}

				step = 4;
			}
			else {
				block();
			}
			break;
		}        
	}

	public boolean done() {
		if (step == 2 && driverUtility > 2000) {
			System.out.println("Attempt failed: Low utility value");
		}
		return ((step == 2 && driverUtility > 2000) || step == 4);
	}
}
