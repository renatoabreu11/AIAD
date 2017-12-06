package behaviours;

import agents.Driver;
import agents.ParkingLot;
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
	private String parkDuration;
	private double price;
	private double driverUtility;
	private MessageTemplate mt; // The template to receive replies
	private int step = 0;
	
	public RequestEntryPerformer(Driver driver, ParkingLot pl) {
		super(driver);
		int parkingDuration = ((Driver) myAgent).getParkingDuration();
		this.parkDuration = String.valueOf(parkingDuration);
		this.parkingAgent = (AID) pl.getAID();
	}

	public void action() {
		switch (step) {
		case 0:
			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
			cfp.addReceiver(parkingAgent);
			
			cfp.setContent(parkDuration);
			cfp.setConversationId("park-entry");
			cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
			myAgent.send(cfp);
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
					if (driverUtility > 0.5) { // we need to check this
						step = 2; 
					}
				}
			}
			else {
				block();
			}
			break;
		case 2:
			ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			order.addReceiver(parkingAgent);
			order.setContent(parkDuration);
			order.setConversationId("park-entry");
			order.setReplyWith("entry "+System.currentTimeMillis());
			myAgent.send(order);

			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("park-entry"),
					MessageTemplate.MatchInReplyTo(order.getReplyWith()));
			step = 3;
			break;
		case 3:      
			reply = myAgent.receive(mt);
			if (reply != null) {
				// Entry reply received
				if (reply.getPerformative() == ACLMessage.INFORM) {
					// Entry successful.
					System.out.println("Driver " + myAgent.getName() + " successfully parked at " + reply.getSender().getName());
					System.out.println("Price = " + price + "\nParking duration: " + parkDuration );
				}
				else {
					System.out.println("Park entry failed: park at maximum capacity");
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
		if (step == 2 && driverUtility < 0.5) {
			System.out.println("Attempt failed: Low utility value");
		}
		return ((step == 2 && driverUtility < 0.5) || step == 4);
	}
}
