import java.util.*;

/*
 *  Utility class for abstracting away the priorityqueue
 */

public class EventQueue {

	private PriorityQueue<Event> pq = new PriorityQueue<Event>();

	public double currentTime = 0.0;

	public void add(Event e) {
		assert e.time >= currentTime;		
		pq.add(e);
	}

	public Event next() {
		Event e = pq.poll();
		assert e != null;

		currentTime = e.time;

		return e;
	}

	public Event peek() {
		return pq.peek();
	}

	public int size() {
		return pq.size();
	}

	public boolean empty() {
		return size() == 0;
	}

	public String toString() {
		return pq.toString();
	}
	

	/** For testing **/
	public static void main(String[] args) {	
		Node n1 = new Node(1);
		Event e1 = Event.PacketReady(n1, 1);
		Event e2 = Event.PacketReady(n1, 2);
		Event e3 = Event.PacketReady(n1, 0);

		EventQueue eq = new EventQueue();
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