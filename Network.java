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
			Node node = new Node(n, backoffAlgorithm);
			topology.put(node, pos);

			if (backoffAlgorithm == Node.IDEAL_BOGGS) {
				node.numMachines = numNodes;
			}

			else if (backoffAlgorithm == Node.IDEAL_IDLE_SENSE) {
				node.optimalCW = (int) (2.0 / Constants.PE_OPT[numNodes-1]);
			}
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

    public double avgPacketSize() {
    	double size = 0.0;

    	for (Node node : getMachines()) {
    		size += node.stats.bitsSent;
    	}

    	return size / getMachines().size();
    }

    public double excessTransmissionDelay() {
    	double otherMachines = getMachines().size() - 1;
    	double ideal = otherMachines * (avgPacketSize() + Constants.PREAMBLE_TIME + Constants.INTERPACKET_GAP);
    	return getTransmissionDelay() - ideal;
    }

    public double getJains() {
    	double sum_xi = 0.0;
    	double sum_xi_sq = 0.0;

    	for (Node node : getMachines()) {
    		sum_xi += node.throughput();
    		sum_xi_sq += node.throughput() * node.throughput();
    	}

    	return sum_xi * sum_xi / (getMachines().size() * sum_xi_sq);
    }

	public static void main(String[] args) throws IOException {
		int iterations = 1000000;
		/*
		int[] eps =       { 1, 2, 3, 4, 5, 7, 9, 14, 18, 25 };
		double[] alphas = { .95, .955, .96, .965, .97, .975, .98, .985, .99 };
		double[] betas =  { .5 , .65, .7, .725, .75, .775, 1.0};
		int[] gammas = 	  { 1, 2, 3, 4, 5, 6 };
		boolean[] halved =  { true, false};
		*/
		int[] eps = {8,10,12,14};
		double[] alphas = {.98,.985, .99, .995};
		double[] betas = {.5, .65,.725, .85,1.0};
		int[] gammas = 	  {2, 3, 4};
		boolean[] halved =  { true, false};

		int byteCount = 64;
		int nodes = 7;
	
		Constants.MAX_PACKET_SIZE = byteCount * 8;
		Constants.MAX_TRANS = Math.max(nodes, 5);
		Constants.IDLE_TARGET = Constants.IDLE_AVG_OPT_HALF[nodes-1];


		PrintWriter writer = new PrintWriter("Constants.csv", "UTF-8");
		writer.println("eps,alphas,betas,gammas,halved,PacketsPerSecond");


		for(int e : eps) {
			for(double a : alphas) {
				for(double b : betas) {
					for(int g: gammas) {
						for (boolean half : halved) {
							System.out.format("%d,%f,%f,%d\n",e,a,b,g);

							Constants.EPS   = e;
							Constants.ALPHA = a;
							Constants.BETA  = b;
							Constants.GAMMA = g;
							if (half) {
								Constants.IDLE_TARGET = Constants.IDLE_AVG_OPT_HALF[nodes-1];
							}
							else {
								Constants.IDLE_TARGET = Constants.IDLE_AVG_OPT[nodes-1];
							}


							Map<Node, Integer> topology = Network.generateTopology(nodes, Node.IDLE_SENSE);
							Network net = new Network(topology);
							net.simulate(iterations * nodes);
							if (half) {
								writer.format("%d,%f,%f,%d,halved,%f\n",e,a,b,g,net.getPacketsPerSecond());
							}
							else {
								writer.format("%d,%f,%f,%d,full,%f\n",e,a,b,g,net.getPacketsPerSecond());

							}
						}
					}	
				}
			}
		}
		writer.close();
	}				
}
