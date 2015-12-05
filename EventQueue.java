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

	}
}