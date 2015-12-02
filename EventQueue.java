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

	public void add(Event e) {
		assert e.time >= currentTime;
		// temporary until a cleaner way is found
		assert e.dest == null;

		for (Node node : nodes) {

		}
		pq.add(e);
	}

	public Event next() {
		Event e = pq.poll();
		assert e != null;

		return e;
	}

	/** For testing **/
	public static void main(String[] args) {
		Node n1 = new Node(1, 2);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.add(n1);
		
		EventQueue eq = new EventQueue(nodes);

		Event e1 = Event.PacketReadyEvent(1, n1);
		Event e2 = Event.PacketReadyEvent(2, n1);
		Event e3 = Event.PacketReadyEvent(0, n1);

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