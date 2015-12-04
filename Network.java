import java.util.*;

public class Network {

	private Map<Node, Integer> topology;
	private EventQueue eventQueue;
	private double currentTime;

	public Network(Map<Node, Integer> topology) {
		this.topology = topology;
		eventQueue = new EventQueue();
		currentTime = 0.0;
	}

	private Collection<Node> getMachines() {
		return topology.keySet();
	}

	public void nextEvent() {
		while (!eventQueue.empty()) {
			Event event = eventQueue.next();
			Event react = event.dest.react(event);
			generateRelativeEvents(react);
		}
	}

	/* For an event spawned by machine n, there are really 
	 * a bunch of relative events corresponding to when that 
	 * event reaches all the other nodes, since they're not 
	 * equidistant
	 */
	private void generateRelativeEvents(Event e) {
		for (Node dest : getMachines()) {
			if (dest != e.source) { // don't send to self...
				eventQueue.add(adjustEvent(e, dest));
			}
		}
	}

	/* 
	 * Adjust event to be in terms of dest node 
	 */
	private Event adjustEvent(Event e, Node dest) {
		assert e.dest == null;
		assert dest != e.source;

		Event rel = e.copy(); // relative event
		rel.dest = dest;
		int dist = 0;//rel.source.distanceTo(rel.dest);
		double propTime = 100000 * dist; // todo: fix me !!!

		assert false;
		return rel;
	}	




	public static void main(String[] args) {
		// Collection<Node> nodes = new ArrayList<Node>();		
		// EventQueue eq = new EventQueue(nodes);

		// int[] positions = {1, 3, 5, 7}; // this should be some hash

		
		// need to abstract away the topology. perhaps rename this
		// to be liek a 'network' class which has the eventqueue,
		// and takes over some of the functionality of eventqueue 
		// (like the subevent splitting). 
		// it should hold the topology (a hash from node to position)
		// here, and use that to spawn subevents
		// eventqueue should JUST be a wrapper really
		

		// for (int i = 0; i < positions.length; i++) {
		// 	Node node = new Node(i, positions[i]);
		// 	nodes.add(node);
		// 	assert node.state == State.PREPARING_NEXT_PACKET;
		// }
	}
}