import java.util.*;

public class Network {

	private Map<Node, Integer> topology;
	private EventQueue eventQueue;
	private Map<Node, Event> nextEvent = new HashMap<Node, Event>();

	private double currentTime;

	public Network(Map<Node, Integer> topology) {
		this.topology = topology;
		eventQueue = new EventQueue();
		currentTime = 0.0;
	}

	private Collection<Node> getMachines() {
		return topology.keySet();
	}

	private void nextEventWithDest(Node node) {

	}

	public void simulate() {
		System.out.println(this);
		init();
		System.out.println("After");
		System.out.println(this);
		

		while (!eventQueue.empty()) {
			Event event = eventQueue.next();
			Event reaction = event.dest.react(event);
			generateRelativeEvents(reaction);
		}
	}

	/* 
	 * Move each machine into a PREPARING_PACKET state
	 */
	private void init() {
		for (Node machine : getMachines()) {
			add(machine.start());
		}
	}

	private void add(Event e) {
		if (!nextEvent.containsKey(e.dest) || 
			nextEvent.get(e.dest).time > e.time) {
			nextEvent.put(e.dest, e);
		}
		eventQueue.add(e);
	}

	/* For an event spawned by machine n, there are really 
	 * a bunch of relative events corresponding to when that 
	 * event reaches all the other nodes, since they're not 
	 * equidistant
	 */
	private void generateRelativeEvents(Event e) {
		for (Node dest : getMachines()) {
			if (dest != e.source) { // don't send to self...
				add(adjustEvent(e, dest));
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

	public String toString() {
		String s = String.format("Time: %f\n", currentTime);

		for (Node m : getMachines()) {
			String nes = "[no next event]";
			if (nextEvent.containsKey(m)) {
				nes = nextEvent.get(m).toString();
			}
			s += String.format("[pos%d %s] next:[%s]\n", topology.get(m), m, nes);
		}

		return s;
	}


	public static void main(String[] args) {
		Map<Node, Integer> topology = new HashMap<Node, Integer>();
		topology.put(new Node(1), 1);
		topology.put(new Node(2), 4);

		Network net = new Network(topology);
		
		net.simulate();

	}
}