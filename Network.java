import java.util.*;

public class Network {

	private Map<Node, Integer> topology;
	
	private EventQueue eventQueue = new EventQueue();
	
	private Map<Node, Event> nextEvent = new HashMap<Node, Event>();

	private Statistics stats = new Statistics();

	private double currentTime = 0.0;

	public Network(Map<Node, Integer> topology) {
		this.topology = topology;
	}

	private Collection<Node> getMachines() {
		return topology.keySet();
	}

	public void simulate() {
		init();

		while (!eventQueue.empty()) {
			Event event = eventQueue.next();
			currentTime = event.time;

			Action action = event.dest.react(event);

			// Might not do anything if, say, event is PacketReady
			if (action != null) {
				if (action.actionType == ActionType.SEND_PREAMBLE) {
					spawnPreambleEvents(action);
				} /*else if (action.actionType == ActionType.SEND_PACKET) {
					//sendPacket(action);
				} else if (action.actionType == ActionType.SEND_JAMMING) {
					//sendJamming(action);
				} else if (action.actionType == ActionType.BACKOFF) {
					//backoff(action);
				}*/ else {
					System.out.println("We something");
					assert false;
				}
			}
		}
	}

	private void init() {
		for (Node machine : getMachines()) {
			add(machine.start());
		}
	}	

	private void spawnPreambleEvents(Action action) {
		for (Node dest : getMachines()) {

			double startTime = currentTime;
			startTime += timeToReach(action.source, dest);

			Event start = Event.PreambleStart(action.source, startTime);
			Event end = Event.PreambleEnd(action.source, startTime + action.duration);

			add(start);
			add(end);
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

	private double timeToReach(Node source, Node dest) {
        double sourcePos = (double) topology.get(source);
        double destPos   = (double) topology.get(dest);

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