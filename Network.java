import java.util.*;
import java.io.PrintWriter;
import java.io.IOException;

public class Network {

	private Map<Node, Integer> topology;

	private EventQueue eventQueue = new EventQueue();

	private Statistics stats = new Statistics();

	private double currentTime = 0.0;

	private double waitTime = 0.0;

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

	public void simulate(double n) {
		init();

		while (!eventQueue.empty() && n > currentTime) {
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
		waitTime += action.duration;
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

	public int getTotalPackets(){
		int total_packets = 0;

		for (Node node : getMachines()) {
	    	total_packets += node.stats.successfulPackets;
		}
		return total_packets;
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

		// this is not entirely correct
		public double getTransmissionDelaySD(){
			double sd = 0.0;
			double mean = getTransmissionDelay()/getMachines().size();
			for (Node node: getMachines()){
					int successfulPackets = node.stats.successfulPackets;
					double bitTime = currentTime / successfulPackets;
					double transmissionDelay = bitTime / 10000;
					sd += Math.pow(transmissionDelay - mean,2);
			}
			sd /= (getMachines().size()-1);
			return Math.pow(sd,0.5);
		}

    public double avgPacketSize() {
    	double size = 0.0;

    	for (Node node : getMachines()) {
    		size += node.stats.bitsSent;
    	}

    	return size / getMachines().size();
    }

    public double excessTransmissionDelay() {
    	double ideal = (Constants.MAX_PACKET_SIZE + Constants.PREAMBLE_TIME + Constants.INTERPACKET_GAP) * getMachines().size() ;
    	return (getTransmissionDelay() * 10000 - ideal);
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
		int[] iterations = new int[199];
		for (int a = 1; a < 200; a++){
			iterations[a - 1] = (a * a * 6000 / 4);
		}

		int nodes = 16;
		int byteCount = 1024;
        Constants.MAX_PACKET_SIZE = byteCount * 8;
        Constants.MAX_TRANS = Math.max(nodes, 5);
		Constants.IDLE_TARGET = Constants.IDLE_AVG_OPT_HALF[nodes-1];

		PrintWriter IDLEfig13 = new PrintWriter("IDLEfig13.csv", "UTF-8");
		PrintWriter EXOfig13 = new PrintWriter("EXOfig13.csv", "UTF-8");
		PrintWriter BOGGSfig13 = new PrintWriter("BOGGSfig13.csv", "UTF-8");

		IDLEfig13.println("WindowSize,Fairness");
		EXOfig13.println("WindowSize,Fairness");
		BOGGSfig13.println("WindowSize,Fairness");

		for (int numIteration : iterations){
			System.out.format("Running %d iterations\n", numIteration);
			Map<Node, Integer> topology = Network.generateTopology(nodes, Node.IDLE_SENSE);
            Network net = new Network(topology);
            net.simulate(numIteration);

            IDLEfig13.format("%f,%f\n", net.currentTime / 10000000.0,net.getJains());
            System.out.format("%f\n",net.currentTime / 10000000.0);


		}

		for (int numIteration : iterations){
			System.out.format("Running %d iterations\n", numIteration);
			Map<Node, Integer> topology = Network.generateTopology(nodes, Node.EXPONENTIAL_BACKOFF);
            Network net = new Network(topology);
            net.simulate(numIteration);

            EXOfig13.format("%f,%f\n", net.currentTime /10000000.0,net.getJains());
		}


		for (int numIteration : iterations){
			System.out.format("Running %d iterations\n", numIteration);
			Map<Node, Integer> topology = Network.generateTopology(nodes, Node.IDEAL_BOGGS);
            Network net = new Network(topology);
            net.simulate(numIteration);

            BOGGSfig13.format("%f,%f\n", net.currentTime / 10000000.0,net.getJains());
		}

		IDLEfig13.close();
		EXOfig13.close();
		BOGGSfig13.close();
	}
}
