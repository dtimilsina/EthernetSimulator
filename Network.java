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
			System.out.format("Network: \n%s\n", this);
			Event event = eventQueue.next();
			currentTime = event.time;

			Action action = event.dest.react(event);

			// Might not do anything if, say, event is PacketReady
			if (action != null) {
				switch (action.actionType) {
					case PREPARE_PACKET: 
						preparePacketEvent(action); 
						break;
					case WAIT:
						waitEvent(action); 
						break;
					default:
						spawnEvents(action);
				}
			}
		}
	}

	private void init() {
		for (Node machine : getMachines()) {
			Action action = machine.start();
			Event event = new Event(EventType.PACKET_READY,
									machine,
									machine,
									currentTime + action.duration);
			add(event);
		}
	}

	private void preparePacketEvent(Action action) {
		double time = currentTime + action.duration;
		Event event = new Event(EventType.PACKET_READY, action.source, action.source, time);
		add(event);
	}

	private void spawnEvents(Action action) {
		for (Node dest : getMachines()) {

			double startTime = currentTime;
			startTime += timeToReach(action.source, dest);

			EventType startType = null;
			EventType endType = null;

			switch (action.actionType) {
				case SEND_PREAMBLE:
					startType = EventType.PREAMBLE_START;
					endType = EventType.PREAMBLE_END;
					break;
				case SEND_PACKET:
					startType = EventType.PACKET_START;
					endType = EventType.PACKET_END;
					break;
				case SEND_JAMMING:
					startType = EventType.JAMMING_START;
					endType = EventType.JAMMING_END;
					break;
				default:
					System.out.println("FUCK");
					System.out.println(action.actionType.name());
					System.exit(1);
			}

			Event start = new Event(startType, action.source, dest, startTime);
			Event end = new Event(endType, action.source, dest, startTime + action.duration);

			add(start);
			add(end);
		}		
	}

	private void waitEvent(Action action) {
		double time = currentTime + action.duration;
		Event event = new Event(EventType.WAIT_END, action.source, action.source, time);
		add(event);
	}

	private void add(Event e) {
		assert e.time >= currentTime;

		if (!nextEvent.containsKey(e.dest) || 
			nextEvent.get(e.dest).time > e.time) {
			nextEvent.put(e.dest, e);
		}

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