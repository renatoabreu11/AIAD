package behaviours;

import agents.parkingLot.ParkingLot;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import sajas.core.AID;
import sajas.core.behaviours.OneShotBehaviour;
import sajas.domain.DFService;

public class ShareWeeklyInfoPerformer extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5420622522313214506L;

	/**
	 * Behavior that executes once a week and sends the weekly info to all the parks
	 * @param a
	 * @param period
	 */

	@Override
	public void action() {
		ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
		cfp.setConversationId("park-cooperation");
		cfp.setReplyWith("cfp"+System.currentTimeMillis());
		((ParkingLot) myAgent).logMessage("Weekly info share performed\n" + cfp);
		
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("COOPERATIVE_PARKING_LOT");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(myAgent, template); 
			for (int i = 0; i < result.length; ++i) {
				cfp.addReceiver((AID) result[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		myAgent.send(cfp);
	}
} 