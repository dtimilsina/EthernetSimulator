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
		while (!eventQueue.empty() && i++ < n) {
			//Debug.print("----------", 1);

	       	Event event = eventQueue.next();

			// Filter END events for cancelled packets
			if (isPacketCancelled(event)) {
				//System.out.println("Skipping cancelled " + event);
				continue;
			}

			if (event.dest == event.source && event.isStartEvent()) {
				//System.out.println("Skipping useless " + event);
				continue;
			}

			currentTime = event.time;

			//System.out.format("Network: \n%s\n", this);
			//System.out.format("Next event: %s\n\n", event);

			Action action = event.dest.react(event);

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
		}
	}

	private void waitEvent(Action action) {
		double time = currentTime + action.duration;
		Event event = new Event(EventType.WAIT_END, action.source, action.source, time, action.packetId);
		add(event);
	}

	private void backoffEvent(Action action) {
		//System.out.println(action.source.id + " backingoff " + action.duration);
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

	public double getPacketsPerSecond(){
		int total_packets = 0;
		for (Node node : getMachines()) {
	    	total_packets += node.stats.successfulPackets; 
		}
		double total_seconds = currentTime / 10000000;
		return total_packets / total_seconds;
    }

    public double getTransmissionDelay(){
    	double time_delayed = 0.0;
    	int total_packets = 0;
    	for (Node node : getMachines()) {
	    	time_delayed += node.stats.slotsWaited * Event.SLOT_TIME + 
	    					(1) * (Event.PREAMBLE_TIME + 
	    						   Node.MAX_PACKET_SIZE + 
	    						   Event.INTERPACKET_GAP) +
	    					(node.stats.collisions) * (Event.PREAMBLE_TIME + 
	    												   Event.INTERPACKET_GAP + 
	    												   Node.MAX_PACKET_SIZE /2 + 
	    												   Event.JAMMING_TIME);
	    	total_packets += node.stats.successfulPackets;
		}
		time_delayed = time_delayed / 512 * 51.6 / 1000;
    	//return (time_delayed / total_packets);
		return currentTime * getMachines().size() / total_packets / 512 * 51.6 / 1000;
    }      

	public static void main(String[] args) {
		// Map<Node, Integer> topology = new HashMap<Node, Integer>();
		// topology.put(new Node(1), 0);
		// topology.put(new Node(2), 1000);
		// topology.put(new Node(3), 250);
		// topology.put(new Node(4), 500);
		// topology.put(new Node(5), 750);

		if (args.length > 1) {
			Debug.threshold = Integer.parseInt(args[1]);
		}

		// Map<Node, Integer> topology = Network.generateTopology(12);

		// Network net = new Network(topology);

		// net.simulate(Integer.parseInt(args[0]));

		// net.printStats();
		// System.out.println("DONE");
		/* Test 3.5 */
		int iterations = 1000000;
		//settings.PACKET_FORMULA = NetSettings.PacketSizeFormula.MAX;
		System.out.println("Hosts,Bytes,PacketsPerSecond");

		for (int nodes = 0; nodes < 24; nodes++){
            int[] bytes = {64,128,256,512,768,1024,1536,2048,3072,4000};
            //int[] bytes = {1024};
            for(int byteCount : bytes){
            	Node.MAX_PACKET_SIZE = byteCount * 8;
                Map<Node, Integer> topology = Network.generateTopology(nodes);
                Network net = new Network(topology);                
                
                //System.out.println("Bytes: " + byteCount);
                //System.out.println("MAX_PACKET " + Node.MAX_PACKET_SIZE);
                
                net.simulate(iterations);
                //System.out.format("%d,%d,%f\n", nodes,byteCount,net.getPacketsPerSecond());
            }
		}
		/* end devin test */
	}
}