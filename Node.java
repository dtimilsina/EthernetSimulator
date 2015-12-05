import java.util.*;

public class Node {

	public int id;
	public State state = State.UNINITIALIZED;
    public int openTransmissions = 0;


    public Node(int id) {
		this.id = id;
    }

    public Event start() {
        assert state == State.UNINITIALIZED;

        transitionTo(State.PREPARING_NEXT_PACKET);
        
        return Event.PacketReady(this);
    }

    public Action react(Event e) {
        assert e.dest == this;
        return own(e) ? nextActionInSequence(e) : reactToExternalEvent(e);
    }

    /*
    in    PACKET_READY,
    in    BACKOFF_END;
    in/ex PREAMBLE_START,
    in/ex PREAMBLE_END,
    in/ex PACKET_START,
    in/ex PACKET_END,
    in/ex JAMMING_START,
    in/ex JAMMING_END,
    */

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
            return new Action(ActionType.WAIT, Event.INTERPACKET_GAP, this);
        }

        System.out.println("What else is there...?");
        System.exit(1);
        return null;
    }

    private int openTransmission() {
        return ++openTransmissions;
    }

    private int closeTransmission() {
        return (openTransmissions = Math.max(openTransmissions-1, 0));
    }

    private boolean own(Event e) {
        return e.source == this;
    }

    private Action nextActionInSequence(Event e) {
        assert own(e);
        // react to end of jamming => backoff
        // 
        return null;
    }

    /* this will allow for creating distributions of sizes and such */
    private int nextPacketSize() {
        return 1; // todo: fix me
    }

    private Action handlePacketReady() {
        assert state == State.PREPARING_NEXT_PACKET;

        transitionTo(State.EAGER_TO_SEND); // below is going to fuck up state

        return isLineIdle() ? new Action(ActionType.SEND_PREAMBLE, Event.PREAMBLE_TIME, this) : null;
    }

    private boolean isLineIdle() {
        return openTransmissions == 0;
    }

    private boolean isInterrupt(Event e) {
    	assert e.source != this;
    	return e.doesTransmit() && isTransmitting() && !transmittingPreamble();
    }

    public boolean transmittingPreamble() {
    	return this.state == State.TRANSMITTING_PACKET_PREAMBLE;
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public Action handleInterrupt() {
        transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);
        return new Action(ActionType.SEND_JAMMING, Event.JAMMING_TIME, this);
    }

    public void transitionTo(State newState) {
    	assert this.state != newState;
    	this.state = newState;
    }

    public String toString() {
        return String.format("id:%d state:%s", id, state.name());
    }


    public static void main(String[] args) {

    }
}
