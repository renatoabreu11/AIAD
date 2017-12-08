package behaviours;

import agents.parkingLot.CooperativeParkingLot;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import sajas.core.behaviours.CyclicBehaviour;

public class ShareWeeklyInfoServer extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6924959528814188765L;

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchConversationId("park-cooperation");
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
			((CooperativeParkingLot) myAgent).storeExternalParkInfo(msg.getSender().getName());
		}
		else {
			block();
		}
	}

}
