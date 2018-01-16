package behaviours;

import java.util.ArrayList;
import java.util.HashMap;

import agents.Agent.Type;
import agents.parkingLot.CooperativeParkingLot;
import agents.parkingLot.ParkingLot;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import parkingLot.Initializer;
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

	public ShareWeeklyInfoServer(AID myAgentAID) {
		ArrayList<ParkingLot> agents = Initializer.agentManager.getParkingAgents();
		for(ParkingLot pl : agents) {
			if(pl.getType().equals(Type.COOPERATIVE_PARKING_LOT)) {
				AID aid = (AID) pl.getAID();
				if(!aid.equals(myAgentAID))
					senders.add(aid);
			}
		}
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchConversationId("park-cooperation");
		ACLMessage msg = myAgent.receive(mt);
		
		
		if(parksWeeklyInfo.size() == senders.size()) {
			((CooperativeParkingLot) myAgent).updatePricingScheme(parksWeeklyInfo);
			parksWeeklyInfo = new HashMap<>();
		}
		
		if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
			try{
				WeeklyInfo wi = (WeeklyInfo) msg.getContentObject();
				String name = msg.getSender().getName();
				parksWeeklyInfo.put(name, wi);
			} catch(UnreadableException e){}
			
		}
		else {
			block();
		}
	}

}
