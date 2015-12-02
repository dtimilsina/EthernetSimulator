import java.util.*;

/*
 *  Utility class for abstracting away the priorityqueue
 */

public class EventQueue {

	private PriorityQueue<Event> pq = new PriorityQueue<Event>();
	private Collection<Node> nodes = new ArrayList<Node>();

	public double currentTime = 0.0;

	public EventQueue(Collection<Node> nodes) {
		this.nodes = nodes;
	}

	/*
	 * Current logic: node n submits its dest-less event to the
	 * queue, ie it's just "putting it out there", using its own
	 * time as a reference. The queue then knows where all the other
	 * machines are, so it takes this event, and enqueues the real 
	 * subevents with destination machines and appropriate timestamps
	 */
	public void add(Event e) {
		assert e.time >= currentTime;

		if (e.doesConcernOthers()) {
			generateRelativeEvents(e);
		} 

		else { 
			// maybe. havent thought much about it yet
			pq.add(e);
		}
	}

	/* For an event spawned by machine n, there are really 
	 * a bunch of relative events corresponding to when that 
	 * event reaches all the other nodes, since they're not 
	 * equidistant
	 */
	private void generateRelativeEvents(Event e) {
		for (Node dest : nodes) {
			if (dest != e.source) { // don't send to self...
				pq.add(adjustEvent(e, dest));
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

	public Event next() {
		Event e = pq.poll();
		assert e != null;

		currentTime = e.time;

		return e;
	}

	/** For testing **/
	public static void main(String[] args) {
		Node n1 = new Node(1, 2);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.add(n1);
		
		EventQueue eq = new EventQueue(nodes);

		Event e1 = Event.PacketReady(1, n1);
		Event e2 = Event.PacketReady(2, n1);
		Event e3 = Event.PacketReady(0, n1);

		eq.add(e1); 
		eq.add(e2); 
		eq.add(e3);

		// Verify values earlier in queue
		assert eq.next() == e3;
		assert eq.next() == e1;
		assert eq.next() == e2;
		assert eq.next() == null;
	}
}