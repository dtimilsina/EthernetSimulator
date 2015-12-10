import java.util.*;
import java.io.PrintWriter;
import java.io.IOException;

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
	       	Event event = eventQueue.next();

			if (isPacketCancelled(event)) {
				continue;
			}

			if (event.dest == event.source && event.isStartEvent()) {
				continue;
			}

			currentTime = event.time;

			Action action = event.dest.react(event);

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
		return event.eventType == EventType.PACKET_END &&
			cancelledPackets.get(event.source).contains(event.packetId);
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

        return Math.abs(sourcePos - destPos) / Constants.PROPAGATION_SPEED;
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

	public static Map<Node, Integer> generateTopology(int numNodes, int backoffAlgorithm) {
		Map<Node, Integer> topology = new HashMap<Node, Integer>();

		for (int n = 0; n < numNodes; n++) {
			int pos = 1000*(n % 4) + 25*(n / 4);
			topology.put(new Node(n, backoffAlgorithm), pos);
		}

		return topology;
	}

	public double getPacketsPerSecond() {
		int total_packets = 0;

		for (Node node : getMachines()) {
	    	total_packets += node.stats.successfulPackets;
		}

		double total_seconds = currentTime / 10000000;

		return total_packets / total_seconds;
    }

    public double getTransmissionDelay(){
    	int total_packets = 0;

    	for (Node node : getMachines()) {
	    	total_packets += node.stats.successfulPackets;
		}
		double bitTimePerPacket = currentTime * getMachines().size() / total_packets;
		double transmissionDelay = bitTimePerPacket / 10000;
		return transmissionDelay;
    }

	public static void main(String[] args) throws IOException {
		int iterations = 1000000;

		if (args.length > 0) {
			iterations = Integer.parseInt(args[0]);
		}

		System.out.format("Running %d iterations\n", iterations);

		//settings.PACKET_FORMULA = NetSettings.PacketSizeFormula.MAX;

		PrintWriter write3_3 = new PrintWriter("data_3-3.csv", "UTF-8");
		PrintWriter write3_5 = new PrintWriter("data_3-5.csv", "UTF-8");
		PrintWriter write3_7 = new PrintWriter("data_3-7.csv", "UTF-8");

		write3_3.println("Hosts,Bytes,PacketsPerSecond");
		write3_5.println("Hosts,Bytes,PacketsPerSecond");
		write3_7.println("Hosts,Bytes,PacketsPerSecond");

		for (int nodes = 1; nodes <= 24; nodes++){

            //int[] bytes = { 64, 128, 256, 512, 768, 1024, 1536, 2048, 3072, 4000 };
            int[] bytes = { 200 };

            for(int byteCount : bytes) {
            	Constants.MAX_PACKET_SIZE = byteCount * 8;

                Map<Node, Integer> topology = Network.generateTopology(nodes, Node.IDLE_SENSE);

                Network net = new Network(topology);

				Constants.MAX_TRANS = Math.max(nodes, 5);
				Constants.nIdleTarget = Constants.nIdleAvgOpt[nodes-1];

                net.simulate(iterations);

                int bits = 8 * byteCount;
                double used = bits + Constants.PREAMBLE_TIME + Constants.INTERPACKET_GAP;
                double mbits = used / Math.pow(10, 6);
                double totalBitRate = net.getPacketsPerSecond() * mbits;

                write3_3.format("%d,%d,%f\n", nodes, byteCount, totalBitRate);
                write3_5.format("%d,%d,%f\n", nodes,byteCount,net.getPacketsPerSecond());
                write3_7.format("%d,%d,%f\n", nodes,byteCount,net.getTransmissionDelay());

                System.out.println("For " + nodes);
				for (Node node : net.getMachines()) {
					System.out.println(node.nIdleAvg);
				}
            }
		}

		write3_3.close();
		write3_5.close();
		write3_7.close();
	}
}
