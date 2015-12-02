import java.util.*;

/*
 *  Utility class for abstracting away the priorityqueue
 */

public class EventQueue {

	private PriorityQueue<Event> pq;

	public EventQueue() {
		pq = new PriorityQueue<Event>();
	}

	public void add(Event e) {
		pq.add(e);
	}

	public Event next() {
		Event e = pq.poll();
		assert e != null;

		return e;
	}

	/** For testing **/
	public static void main(String[] args) {
		EventQueue eq = new EventQueue();

		Node n1 = new Node(1, 2, eq), n2 = new Node(3,4, eq);
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