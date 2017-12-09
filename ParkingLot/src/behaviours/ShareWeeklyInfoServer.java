package behaviours;


import java.util.ArrayList;
import java.util.HashMap;

import agents.parkingLot.CooperativeParkingLot;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import sajas.core.AID;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.domain.DFService;
import utils.WeeklyInfo;

public class ShareWeeklyInfoServer extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6924959528814188765L;

	private ArrayList<AID> senders = new ArrayList<>();
	private HashMap<String, WeeklyInfo> parksWeeklyInfo = new HashMap<>();

	public void ShareInfoServer() {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("COOPERATIVE_PARKING_LOT");
		template.addServices(sd);
		DFAgentDescription[] result;
		try {
			result = DFService.search(myAgent, template);
			for (int i = 0; i < result.length; ++i) {
				AID aid = (AID) result[i].getName();
				if(!aid.equals(myAgent.getAID()))
					senders.add(aid);
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchConversationId("park-cooperation");
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
			try{
				WeeklyInfo wi = (WeeklyInfo) msg.getContentObject();
				System.out.println(wi.getTotalProfit());
				String name = msg.getSender().getName();
				parksWeeklyInfo.put(name, wi);
			} catch(UnreadableException e){}
			
			if(parksWeeklyInfo.size() == senders.size()) {
				((CooperativeParkingLot) myAgent).updatePricingScheme(parksWeeklyInfo);
				parksWeeklyInfo = new HashMap<>();
			}
		}
		else {
			block();
		}
	}

}
