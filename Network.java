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
			Event reaction = event.dest.react(event);
			generateRelativeEvents(reaction);
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

	}
}