import java.util.*;

public class Network {

	private Map<Node, Integer> topology;
	
	private EventQueue eventQueue = new EventQueue();
	
	private Statistics stats = new Statistics();

	private double currentTime = 0.0;

	private Map<Node, Set<Integer>> cancelledPackets;


	public Network(Map<Node, Integer> topology) {
		this.topology = topology;
		 cancelledPackets = new HashMap<Node, Set<Integer>>();

		 for (Node node : getMachines()) {
		 	cancelledPackets.put(node, new HashSet<Integer>());
		 }
	}

	private Collection<Node> getMachines() {
		return topology.keySet();
	}

	public void simulate(int n) {
		init();

		int i = 0;
		while (!eventQueue.empty()){// && i++ < n) {
			//System.out.println("---------");
			//System.out.println("q" + eventQueue);

	       	Event event = eventQueue.next();

	       	if (event.eventType == EventType.JAMMING_END) {
	       		System.out.println("@@@@@jammend");
	       	}
			// Filter END events for cancelled packets
			if (isPacketCancelled(event)) {
				System.out.println("x CANCELLED PACKET");
				System.out.println("   " + event);
//				System.exit(0);
				continue;
			}

			if (event.dest == event.source && event.isStartEvent()) {				
				continue;
			}

			currentTime = event.time;

			//System.out.format("Network: \n%s\n", this);
			//System.out.format("Next event: %s\n\n", event);

			Action action = event.dest.react(event);

			if (event.eventType == EventType.JAMMING_END){// && event.source == event.dest) {
				System.out.println("JAMMING_END encountered");
				System.out.println("Event: " + event);
				System.out.println("Reaction: " + action);
				//assert action.actionType == ActionType.BACKOFF : action.actionType.name();
			} 

			else if (event.eventType == EventType.JAMMING_START) {
				System.out.println("START JAMMING");
			}

			// Might not do anything if, say, event is PacketReady
			if (action != null) {
				switch (action.actionType) {
					case PREPARE_PACKET: 
						packetReadyEvent(action); 
						break;
					case WAIT:
						waitEvent(action); 
						break;
					case BACKOFF:
						backoffEvent(action);
						break;
					case SEND_JAMMING:
						cancelPackets(action);
						spawnEvents(action);
						break;
					default:
						spawnEvents(action);
				}
			}

			else {
				//System.out.println("NULL REACTION TO:");
				//System.out.println("  event: " + event);
			}			
		}
	}

	private void init() {
		for (Node machine : getMachines()) {
			Action action = machine.start();
			Event event = new Event(EventType.PACKET_READY,
									machine,
									machine,
									currentTime + action.duration,
									action.packetId);
			add(event);
		}
	}

	private boolean isPacketCancelled(Event event) {
		// preamble_end and jamming_start could become reordered
		// just due to tiebreaker
		return (event.eventType == EventType.PACKET_END ||
				event.eventType == EventType.PREAMBLE_END) 
			&& cancelledPackets.get(event.source).contains(event.packetId);
	}

	/* Mark as cancelled any event that is associated with 
	   this packetId from this source */
	private void cancelPackets(Action action) {
		//System.out.println("Canceled by action: " + action);
		//System.out.println("  Cancelling packet m" + action.source.id + " p" + action.packetId);
		cancelledPackets.get(action.source).add(action.packetId);
	}

	private void packetReadyEvent(Action action) {
		double time = currentTime + action.duration;
		Event event = new Event(EventType.PACKET_READY, 
								action.source, 
								action.source, 
								time,
								action.packetId);
		add(event);
	}

	private void spawnEvents(Action action) {
		for (Node dest : getMachines()) {

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


			double startTime = currentTime;
			startTime += timeToReach(action.source, dest);

			Event start = new Event(startType,
									action.source,
									dest, 
									startTime,
									action.packetId);

			Event end = new Event(endType, 
								  action.source, 
								  dest, 
								  startTime + action.duration,
								  action.packetId);

			int size = eventQueue.size();
			add(start);
			add(end);
			if (endType == EventType.JAMMING_END) {
				System.out.println("```````hi " + end.dest.id);
				System.out.println("   " + end);
			}
			assert eventQueue.size() == size + 2 : "wtf size";
		}
	}

	private void waitEvent(Action action) {
		double time = currentTime + action.duration;
		Event event = new Event(EventType.WAIT_END, action.source, action.source, time, action.packetId);
		add(event);
	}

	private void backoffEvent(Action action) {
		System.out.println(action.source.id + " backingoff " + action.duration);
		double time = currentTime + action.duration;
		Event event = new Event(EventType.BACKOFF_END, action.source, action.source, time, action.packetId);
		add(event);
	}

	private void add(Event e) {
		assert e.time >= currentTime;
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
			s += String.format("[%s pos%d]\n", m, topology.get(m));
		}

		return s;
	}

	public void printStats() {
		for (Node node : getMachines()) {
			System.out.format("id %d:", node.id);
			System.out.format("  succ: %d  coll: %d aborted %d\n", node.stats.successfulPackets, node.stats.collisions, node.stats.packetsAborted);
		}
	}

	public static Map<Node, Integer> generateTopology(int numNodes) {
		Map<Node, Integer> topology = new HashMap<Node, Integer>();

		for (int n = 0; n < numNodes; n++) {
			int pos = 1000*(n % 4) + 25*(n / 4);
			topology.put(new Node(n), pos);
		}

		return topology;
	}

	public static void main(String[] args) {
		// Map<Node, Integer> topology = new HashMap<Node, Integer>();
		// topology.put(new Node(1), 0);
		// topology.put(new Node(2), 1000);
		// topology.put(new Node(3), 250);
		// topology.put(new Node(4), 500);
		// topology.put(new Node(5), 750);
		Map<Node, Integer> topology = Network.generateTopology(2);

		Network net = new Network(topology);

		net.simulate(Integer.parseInt(args[0]));

		net.printStats();
	}
}