package behaviours;

public class RequestEntryServer extends CyclicBehaviour {
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			// CFP Message received. Process it
			String title = msg.getContent();
			ACLMessage reply = msg.createReply();

			Integer price = (Integer) catalogue.get(title);
			if (price != null) {
				// The requested book is available for sale. Reply with the price
				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(String.valueOf(price.intValue()));
			}
			else {
				// The requested book is NOT available for sale.
				reply.setPerformative(ACLMessage.REFUSE);
				reply.setContent("not-available");
			}
			myAgent.send(reply);
		}
		else {
			block();
		}
	}
}  // End of inner class OfferRequestsServer
