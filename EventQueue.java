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
		Node n1 = new Node(1, 2, eq), n2 = new Node(3,4, eq);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.add(n1); nodes.add(n2);
		EventQueue eq = new EventQueue(nodes);

		Event e1 = new Event(1, n1, n2);
		Event e2 = new Event(2, n1, n2);
		Event e3 = new Event(0, n1, n2);

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