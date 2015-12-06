import java.util.*;

public class Node {

    private final int MIN_PACKET_SIZE = 512;
    private final int MAX_PACKET_SIZE = 2048; // 12144;

    private final double TRANSMISSION_RATE = 1.0;
	public int id;
	public State state = State.UNINITIALIZED;
    public int openTransmissions = 0;

    private int currentPacketSize;

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
        double duration = Event.samplePacketReadyTime();
        return new Action(ActionType.PREPARE_PACKET, duration, this);
    }

    public Action react(Event e) {
        assert e.dest == this;

        return own(e) ? nextActionInSequence(e) : reactToExternalEvent(e);
    }

    private boolean own(Event e) {
        return e.source == this;
    }

    /* Externally-clocked internal events */
    private Action nextActionInSequence(Event e) {
        assert own(e);

        // make sure e is acceptable given our current state
        //assert isViable(e) : "Got " + e + " when state " + state.name();

        switch (e.eventType) {
            case PACKET_READY:   
                return handlePacketReady();
            case PREAMBLE_START: 
                transitionTo(State.TRANSMITTING_PACKET_PREAMBLE);
                return null;
            case PREAMBLE_END:   
                return handlePreambleEnd();
            case PACKET_START: 
                transitionTo(State.TRANSMITTING_PACKET_CONTENTS);
                return null;
            case PACKET_END:     
                return handlePacketEnd();
            case JAMMING_START:
                transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);
                return null;
            case JAMMING_END:    
                return handleBackoff();
            case WAIT_END:       
                return handleWaitEnd();
            case BACKOFF_END:    
                return handleBackoffEnd();
            default:
                assert false : "Unknown event type " + e;
                return null;
        }
    }

    private boolean isViable(Event e) {
        switch (e.eventType) {
            case PACKET_READY:
            case PREAMBLE_START: return state == State.EAGER_TO_SEND || 
                                        state == State.WAITING_FOR_BACKOFF ||
                                        state == State.PREPARING_NEXT_PACKET;
            case PREAMBLE_END:   return state == State.TRANSMITTING_PACKET_PREAMBLE;
            case PACKET_START:   return state == State.TRANSMITTING_PACKET_PREAMBLE;
            case PACKET_END:     return state == State.TRANSMITTING_PACKET_CONTENTS;
            case JAMMING_START:  return state == State.TRANSMITTING_PACKET_CONTENTS ||
                                        state == State.TRANSMITTING_PACKET_PREAMBLE;
            case JAMMING_END:    return state == State.TRANSMITTING_JAMMING_SIGNAL;
            case BACKOFF_END:    return state == State.WAITING_FOR_BACKOFF;
            case WAIT_END:       return state == State.WAITING_INTERPACKET_GAP;
            default:             return false;
        }
    }

    private Action handlePacketReady() {
        assert state == State.PREPARING_NEXT_PACKET : state.name();

        return sendIfIdle();
    }

    private Action handleWaitEnd() {
        assert state == State.WAITING_INTERPACKET_GAP : state.name();

        return sendIfIdle();
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
            //transitionTo(State.TRANSMITTING_PACKET_CONTENTS);
            double transmissionTime = currentPacketSize / TRANSMISSION_RATE;
            return new Action(ActionType.SEND_PACKET, transmissionTime, this);
        } else {
            return handleInterrupt();
        }        
    }

    private Action handleBackoff() {
        assert state == State.TRANSMITTING_JAMMING_SIGNAL : state.name();
        assert timesBackedOff <= 16;

        if (timesBackedOff == 16) {
            stats.addAbort();
            timesBackedOff = 0;
            return prepareNextPacket();
        }

        else {
            int slots = nextBackoffSlots();

            stats.addSlotsWaited(slots);

            transitionTo(State.WAITING_FOR_BACKOFF);

            double duration = slots * Event.SLOT_TIME;
            timesBackedOff++;
            return new Action(ActionType.BACKOFF, duration, this);
        }
    }

    private int nextBackoffSlots() {
        int maxWait = timesBackedOff < 10 ? (int) Math.pow(2, timesBackedOff) : 1024;
        return rand.nextInt(maxWait);
    }

    private Action sendIfIdle() {
        assert !state.isTransmittingState() : state.name();

        if (isLineIdle()) {
            //transitionTo(State.TRANSMITTING_PACKET_PREAMBLE);
            return new Action(ActionType.SEND_PREAMBLE, Event.PREAMBLE_TIME, this);
        }

        else {
            transitionTo(State.EAGER_TO_SEND);
            return null;
        }
    }

    private Action reactToExternalEvent(Event e) {
        assert !own(e);
        assert e.eventType != EventType.PACKET_READY;

        if (isInterrupt(e)) {
            return handleInterrupt();
        }

        else if (e.eventType == EventType.PREAMBLE_START) {
            openTransmission();
        }

        else if (e.eventType == EventType.PACKET_END) {
            // Note that it is a transmission that may have been closed
            // already due to a jamming sequence
            closeTransmission();
        }

        else if (e.eventType == EventType.JAMMING_START) {
            closeTransmission(); // Close the actual packet expectation
            openTransmission();  // Start jamming sequence transmission
        } 

        else if (e.eventType == EventType.JAMMING_END) {
            closeTransmission(); // End jamming sequence
        }

        if (state == State.EAGER_TO_SEND && isLineIdle()) {
            transitionTo(State.WAITING_INTERPACKET_GAP);
            return new Action(ActionType.WAIT, Event.INTERPACKET_GAP, this);
        } else {
            return null;
        }
    }

    private int openTransmission() {
        return ++openTransmissions;
    }

    private int closeTransmission() {
        return (openTransmissions = Math.max(openTransmissions-1, 0));
    }

    /* this will allow for creating distributions of sizes and such */
    private int nextPacketSize() {
        return rand.nextInt(MAX_PACKET_SIZE - MIN_PACKET_SIZE) + MIN_PACKET_SIZE - (int) Event.PREAMBLE_TIME;
    }

    private boolean isLineIdle() {
        return openTransmissions == 0;// && !isTransmitting();
    }

    private boolean isInterrupt(Event e) {
    	assert e.source != this;
    	return e.doesTransmit() && 
               isTransmitting() && 
               !(state == State.TRANSMITTING_PACKET_PREAMBLE ||
                 state == State.TRANSMITTING_JAMMING_SIGNAL);
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public Action handleInterrupt() {        
        //transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

        stats.addCollision();

        return new Action(ActionType.SEND_JAMMING, Event.JAMMING_TIME, this);
    }

    public void transitionTo(State newState) {    	
        if (this.state == newState) {
            System.out.format("States are %s\n", newState.name());
            //System.exit(1);
        }
    	this.state = newState;
    }

    public String toString() {
        return String.format("id:%d state:%s", id, state.name());
    }


    public static void main(String[] args) {

    }
}
