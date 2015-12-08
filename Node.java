import java.util.*;

public class Node {

	public int id;
	public State state = State.UNINITIALIZED;

    private final int HISTORY_SIZE = 20;
    public LinkedList<EventAction> history = new LinkedList<EventAction>();

    private Set<Node> openTransmissions = new HashSet<Node>();

    private int currentPacketSize;
    public int packetAttempt = 0;

    private int timesBackedOff = 0;

    public Statistics stats = new Statistics();

    Random rand;


    public Node(int id) {
		this.id = id;
        rand = new Random(this.id);
    }

    public Action start() {
        assert state == State.UNINITIALIZED : state.name();;

        return prepareNextPacket();
    }

    private Action prepareNextPacket() {
        transitionTo(State.PREPARING_NEXT_PACKET);
        timesBackedOff = 0;
        currentPacketSize = nextPacketSize();
        double duration = Constants.PACKET_READY_TIME + rand.nextGaussian(); //Event.samplePacketReadyTime();
        return new Action(ActionType.PREPARE_PACKET, duration, this, packetAttempt);
    }

    public Action react(Event e) {
        assert e.dest == this;

        Action reaction = own(e) ? nextActionInSequence(e) : reactToExternalEvent(e);

        record(e, reaction);

        return reaction;
    }

    private void record(Event e, Action reaction) {
        history.add(new EventAction(e, reaction));
        if (history.size() > HISTORY_SIZE) {
            history.removeFirst();
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
                duration = 0.00001;
            }

            timesBackedOff++;
            return new Action(ActionType.BACKOFF, duration, this, packetAttempt);
        }
    }

    private int nextBackoffSlots() {
        int maxWait = timesBackedOff < 10 ? (int) Math.pow(2, 1+timesBackedOff) : Constants.MAX_BACKOFF_SLOTS;
        int slots = rand.nextInt(maxWait);
        return slots;
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
