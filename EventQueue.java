import java.util.*;

/*
 *  Utility class for abstracting away the priorityqueue
 */

public class EventQueue {

	private int eventNum = 0;

	/* For implementing stable sort */
	private class Node implements Comparable<Node> {
		public Event event;
		public int age;

		public Node(Event event, int age) {
			this.event = event;
			this.age = age;
		}

		public int compareTo(Node other) {
			int cmp = new Double(event.time).compareTo(new Double(other.event.time));
			if (cmp == 0) {
				return new Integer(age).compareTo(new Integer(other.age));
			} else {
				return cmp;
			}
		}
	}

	private PriorityQueue<Node> pq = new PriorityQueue<Node>();

	public double currentTime = 0.0;

	public void add(Event e) {
		assert e.time >= currentTime : "Time misorder: " + e + " not >= " + currentTime;
		pq.add(new Node(e, eventNum++));			
	}

	public Event next() {
		Event e = pq.poll().event;
		assert e != null;

		currentTime = e.time;

		return e;
	}

	public Event peek() {
		return pq.peek().event;
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