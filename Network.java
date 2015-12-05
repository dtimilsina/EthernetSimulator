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

	private void nextEventByDest(Node node) {

	}

	public void simulate() {
		System.out.println(this);
		init();

		while (!eventQueue.empty()) {
			System.out.println();
			System.out.format("QUEUE: %s\n", eventQueue);

			Event event = eventQueue.next();
			currentTime = event.time;
			System.out.println("curentime now" + currentTime);			
			
			System.out.format("Event: %s\n", event);

			Event reaction = event.dest.react(event);
			System.out.format("->Reaction: %s\n", reaction);

			if (reaction != null) {
				generateRelativeEvents(reaction);
			}
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
		assert e.time >= currentTime;

		if (!nextEvent.containsKey(e.dest) || 
			nextEvent.get(e.dest).time > e.time) {
			nextEvent.put(e.dest, e);
		}

		System.out.format("Enqueuing %s\n", e);

		eventQueue.add(e);
	}

	/* For an event spawned by machine n, there are really 
	 * a bunch of relative events corresponding to when that 
	 * event reaches all the other nodes, since they're not 
	 * equidistant
	 * CURRENTLY also sending back to the source since it can
	 * use its own events to clock the events' ends
	 * (eg, see its own preamble start, and so schedule the end)
	 */
	private void generateRelativeEvents(Event e) {
		for (Node dest : getMachines()) {
			add(adjustEvent(e, dest));
		}
	}

	/* 
	 * Adjust event to be in terms of dest node 
	 */
	private Event adjustEvent(Event e, Node dest) {
		// need to add to the time provided by e
		assert e.dest == null;

		Event rel = e.copy(); // relative event
		rel.dest = dest;

		double propTime = timeToReachDest(rel);
		rel.time += currentTime + propTime;

		return rel;
	}

	private double timeToReachDest(Event e) {
		if (e.source == e.dest) {
			return 0.0; // careful to avoid any precision errors
		}

        double sourcePos = (double) topology.get(e.source);        
        double destPos   = (double) topology.get(e.dest);

        return Math.abs(sourcePos - destPos) / Event.PROPAGATION_SPEED;
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