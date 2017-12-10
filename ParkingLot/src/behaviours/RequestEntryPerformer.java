package behaviours;

import agents.driver.Driver;
import agents.driver.Driver.DriverState;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import parkingLot.Initializer;
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
	private MessageTemplate mt;
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
			
			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("park-entry"),
					MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
			step = 1;
			break;
		case 1:
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				if (reply.getPerformative() == ACLMessage.PROPOSE) {
					price = Double.parseDouble(reply.getContent());
					
					driverUtility = ((Driver) myAgent).getUtility(price);
					System.out.println("utility: "+driverUtility);
					if(driverUtility < 0.0) {
						((Driver) myAgent).logMessage("Park entry failed: low utility");
						((Driver) myAgent).setState(DriverState.PICKING);
						step = 4;
					} else step = 2;
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
			order.setConversationId("park-accept");
			order.setReplyWith("entry "+System.currentTimeMillis());
			myAgent.send(order);
			
			((Driver) myAgent).logMessage("Accept entry performed\n" + order);

			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("park-accept"),
					MessageTemplate.MatchInReplyTo(order.getReplyWith()));
			step = 3;
			break;
		case 3:      
			reply = myAgent.receive(mt);
			
			if (reply != null) {
				if (reply.getPerformative() == ACLMessage.INFORM) {
					((Driver) myAgent).logMessage("Driver " + myAgent.getName() + " successfully parked at " + reply.getSender().getName() + 
							"\nPrice = " + price + "; Parking duration: " + durationOfStay);
					((Driver) myAgent).setState(DriverState.PARKED);
					Initializer.manager.addUtility(driverUtility);
				}
				else {
					((Driver) myAgent).logMessage("Park entry failed: park at maximum capacity");
					((Driver) myAgent).setState(DriverState.PICKING);
				}
				step = 4;
			} else {
				block();
			}
			break;
		}        
	}

	public boolean done() {
		return (step == 4);
	}
}
