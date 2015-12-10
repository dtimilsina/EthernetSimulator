import java.util.*;

public class Node {

    public static int EXPONENTIAL_BACKOFF = 0;
    public static int IDLE_SENSE = 1;
    public static int IDEAL_BOGGS = 2;
    public static int IDEAL_IDLE_SENSE = 3;

	public int id;
	public State state = State.UNINITIALIZED;

    private final int HISTORY_SIZE = 20;
    public LinkedList<EventAction> history = new LinkedList<EventAction>();

    private Set<Node> openTransmissions = new HashSet<Node>();

    private int currentPacketSize;

    // This is needed for the handling of "dead" packet events which
    // arrive after a packet has been canceled
    public int packetAttempt = 0;

    private int backoffAlgorithm = Node.EXPONENTIAL_BACKOFF;

    // EXPONENTIAL BACKOFF
    private int timesBackedOff = 0; 

    // for 1/Q Boggs ideal
    public int numMachines = 0;


    // IDLE SENSE
    private double startIdleTime = 0.0;
    private int ntrans = 0;
    private double sumIdleSlots = 0.0;
    private int contentionWindow = 0;
    public double nIdleAvg = 0.0;
    private int CUR_MAX_TRANS = Constants.MAX_TRANS;

    // This isn't actually used by the node itself for clocking
    public double currentTime = 0.0;

    public Statistics stats = new Statistics();

    Random rand;


    public Node(int id) {
		this.id = id;
        rand = new Random(this.id);
    }

    public Node (int id, int backoffAlgorithm) {
        this(id);
        this.backoffAlgorithm = backoffAlgorithm;
    }

    public double throughput() {
        return stats.bitsSent / currentTime;
    }

    public Action start() {
        assert state == State.UNINITIALIZED : state.name();;

        return prepareNextPacket();
    }

    private Action prepareNextPacket() {
        transitionTo(State.PREPARING_NEXT_PACKET);

        timesBackedOff = 0;

        currentPacketSize = nextPacketSize();

        double duration = Constants.PACKET_READY_TIME + rand.nextGaussian();

        return new Action(ActionType.PREPARE_PACKET, duration, this, packetAttempt);
    }

    public Action react(Event e) {
        assert e.dest == this;
        
        currentTime = e.time;

        Action reaction = own(e) ? nextActionInSequence(e) : reactToExternalEvent(e);

        record(e, reaction);

        updateIdleSense(e);

        return reaction;
    }

    private void record(Event e, Action reaction) {
        history.add(new EventAction(e, reaction));
        if (history.size() > HISTORY_SIZE) {
            history.removeFirst();
        }
    }

    private void updateIdleSense(Event e) {
        // check causes line to go from idle to busy
        if (e.eventType == EventType.PREAMBLE_START && numberOpenTransmissions() == 1) {
            double idleTime = e.time - startIdleTime;
            double idleSlots = idleTime / Constants.SLOT_TIME;
            sumIdleSlots += idleSlots;            
        } 

        // some transmission ends
        else if (e.eventType == EventType.JAMMING_END || e.eventType == EventType.PACKET_END) {
            ntrans++;

            // line goes idle because of this event
            if (isLineIdle()) {
                startIdleTime = e.time;
            }

            if (ntrans >= CUR_MAX_TRANS) {
                nIdleAvg = sumIdleSlots / ntrans;
                ntrans = 0;
                sumIdleSlots = 0;

                if (nIdleAvg < Constants.nIdleTarget) {
                    /* increase cw additively */
                    contentionWindow = contentionWindow + Constants.EPS;
                } else {
                    /* decrease cw multiplicatively */
                    contentionWindow *= Constants.ALPHA;
                }

                if (Math.abs(Constants.nIdleTarget - nIdleAvg) <= Constants.BETA){
                    CUR_MAX_TRANS = contentionWindow / Constants.GAMMA;
                }
                else{
                    CUR_MAX_TRANS = Constants.MAX_TRANS;
                }
            }
        }
    }

    private boolean own(Event e) {
        return e.source == this;
    }

    /* Externally-clocked internal events */
    private Action nextActionInSequence(Event e) {
        assert own(e);

        switch (e.eventType) {
            case PACKET_READY: return handlePacketReady();                
            case PREAMBLE_END: return handlePreambleEnd();                
            case PACKET_END:   return handlePacketEnd();                
            case JAMMING_END:  return handleBackoff();                
            case WAIT_END:     return handleWaitEnd();                
            case BACKOFF_END:  return handleBackoffEnd();
                
            default:
                assert false : "Unknown event type " + e;
                return null;
        }
    }

    private Action handlePacketReady() {
        assert state == State.PREPARING_NEXT_PACKET : state.name();

        return sendIfIdle();
    }

    private Action handleWaitEnd() {
        assert state == State.WAITING_INTERPACKET_GAP : state.name();

        packetAttempt++;

        transitionTo(State.TRANSMITTING_PACKET_PREAMBLE);

        return new Action(ActionType.SEND_PREAMBLE, Constants.PREAMBLE_TIME, this, packetAttempt);
    }

    private Action handleBackoffEnd() {
        assert state == State.WAITING_FOR_BACKOFF : state.name();

        return sendIfIdle();
    }

    private Action handlePacketEnd() {
        assert state == State.TRANSMITTING_PACKET_CONTENTS : state.name();

        stats.addSuccessfulPacket(currentPacketSize);

        return prepareNextPacket();
    }

    private Action handlePreambleEnd() {
        assert state == State.TRANSMITTING_PACKET_PREAMBLE : state.name();

        if (isLineIdle()) {
            transitionTo(State.TRANSMITTING_PACKET_CONTENTS);
            
            double transmissionTime = currentPacketSize / Constants.TRANSMISSION_RATE;

            return new Action(ActionType.SEND_PACKET, transmissionTime, this, packetAttempt);
        } 

        else {
            return handleInterrupt();
        }        
    }

    private Action handleBackoff() {
        assert state == State.TRANSMITTING_JAMMING_SIGNAL : state.name();
        assert timesBackedOff <= Constants.MAX_BACKOFF_TIMES;

        if (timesBackedOff == Constants.MAX_BACKOFF_TIMES) {
            stats.addAbort();
            timesBackedOff = 0;
            return prepareNextPacket();
        }

        else {
            int slots = nextBackoffSlots();
            stats.addSlotsWaited(slots);

            transitionTo(State.WAITING_FOR_BACKOFF);


            double duration = slots * Constants.SLOT_TIME;
            if (duration < 1) {
                // need to fix this with stable sort but buggy. causes 
                // reorderings of ties that usurp "previous" events
                // though this is physically likely more accurate
                duration = 0.00001;
            }

            timesBackedOff++;
            return new Action(ActionType.BACKOFF, duration, this, packetAttempt);
        }
    }

    private int nextBackoffSlots() {
        //int maxWait = timesBackedOff < 10 ? (int) Math.pow(2, 1+timesBackedOff) : Constants.MAX_BACKOFF_SLOTS;
        //int slots = rand.nextInt(maxWait);
        if (backoffAlgorithm == Node.EXPONENTIAL_BACKOFF) {
            if (timesBackedOff < 10) {
                int maxWait = (int) Math.pow(2, 1 + timesBackedOff);
                return rand.nextInt(maxWait);
            } else {
                return Constants.MAX_BACKOFF_SLOTS;
            }
        } 

        else if (backoffAlgorithm == Node.IDLE_SENSE) {
            if (contentionWindow == 0) return 0;
            return rand.nextInt(contentionWindow); // slots;
        } 

        else if (backoffAlgorithm == Node.IDEAL_BOGGS) {
            return rand.nextInt(numMachines);
        }

        else if (backoffAlgorithm == Node.IDEAL_IDLE_SENSE) {
            assert false : "Need opt CW";
            return 0;
        }
        
        else {
            assert false : "Whoa ho ho there that ain't no algorithm";
            return 0;
        }
    }

    private Action sendIfIdle() {
        assert !state.isTransmittingState() : state.name();

        if (isLineIdle()) {
            packetAttempt++;
            transitionTo(State.TRANSMITTING_PACKET_PREAMBLE);
            return new Action(ActionType.SEND_PREAMBLE, Constants.PREAMBLE_TIME, this, packetAttempt);
        }

        else {
            transitionTo(State.EAGER_TO_SEND);
            return null;
        }
    }

    private Action reactToExternalEvent(Event e) {
        assert !own(e);
        assert e.eventType != EventType.PACKET_READY;

        if (e.eventType == EventType.PREAMBLE_START) {
            openTransmission(e);
        }

        else if (e.eventType == EventType.PACKET_END ||
                 e.eventType == EventType.JAMMING_END) {
            closeTransmission(e.source);
        }

        if (isInterrupt(e)) {
            return handleInterrupt();
        }

        // If line is idle, we'll wait interpacket gap
        // otherwise we'll clock our next transmisison attempt by the 
        // receipt of another end (ie some next time we get to this method)
        if (state == State.EAGER_TO_SEND && isLineIdle()) {
            transitionTo(State.WAITING_INTERPACKET_GAP);
            return new Action(ActionType.WAIT, Constants.INTERPACKET_GAP, this, packetAttempt);
        } else {
            return null;
        }
    }

    private int numberOpenTransmissions() {
        return openTransmissions.size();
    }

    private void openTransmission(Event e) {
        Node source = e.source;
        int packetId = e.packetId;    
        

        if (openTransmissions.contains(source)) {
            System.out.println("DUP PACKET");
            System.out.println("Event: " + e);
            System.out.println("DUP SENDER: " + e.source);
            System.out.println("\nDup sender History:\n-------\n");

            for (EventAction ea : e.source.history) {
                System.out.format("E: %s\nR: %s\n\n", ea.event, ea.action);
            }
        }

        assert !openTransmissions.contains(source) : "Received extra packet " + source.id + "->" + id;

        openTransmissions.add(source);
    }

    private void closeTransmission(Node source) {
        assert openTransmissions.contains(source);

        openTransmissions.remove(source);
    }

    /* this will allow for creating distributions of sizes and such */
    private int nextPacketSize() {
        return Constants.MAX_PACKET_SIZE;
    }

    private boolean isLineIdle() {
        return numberOpenTransmissions() == 0;
    }

    private boolean isInterrupt(Event e) {
    	assert e.source != this;

    	return e.isStartEvent() && state == State.TRANSMITTING_PACKET_CONTENTS;
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public Action handleInterrupt() {        
        transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

        stats.addCollision();

        return new Action(ActionType.SEND_JAMMING, Constants.JAMMING_TIME, this, packetAttempt);
    }

    public void transitionTo(State newState) {    	
        assert this.state != newState : state.name();

    	this.state = newState;
    }

    public String toString() {
        return String.format("m%d state:%s", id, state.name());
    }


    public static void main(String[] args) {

    }
}
