import java.util.*;

public class Node {

	public int id;
	public State state = State.UNINITIALIZED;

    public Collection<Event> openTransmissions;


    public Node(int id) {
		this.id = id;
        openTransmissions = new HashSet<Event>();
    }

    public Event start() {
        assert state == State.UNINITIALIZED;

        transitionTo(State.PREPARING_NEXT_PACKET);
        
        return Event.PacketReady(this);
    }

    public Action react(Event e) {
        assert e.dest == this;

        /* if own(e) => nextActionInSequence(e) */
        if (own(e)) {
            return nextActionInSequence(e);
        }

        else {
            return reactToExternalEvent(e);
        }

        /*
        if (e.eventType == EventType.PACKET_READY) {
            assert own(e);
            return handlePacketReady();
        }

        else if (own(e) && e.eventType == EventType.PREAMBLE_END) {
            transitionTo(State.TRANSMITTING_PACKET_CONTENTS);
            double duration = nextPacketSize() / Event.TRANSMISSION_RATE;
            return new Action(ActionType.SEND_PACKET, duration, this);
        }

        else if (own(e) && e.eventType == EventType.PACKET_END) {
            // successful transmission
        }

        else if (e.eventType == EventType.BACKOFF_END) {

        }

        else if (isInterrupt(e)) {
            return handleInterrupt();
        }

        else if (e.eventType == EventType.PREAMBLE_START) {
            // opent ransmission
        }

        else if (e.eventType == EventType.PREAMBLE_END) {
            // close transmission
        }

        else if (e.eventType == EventType.PACKET_START) {
            // open transmission
        }

        else if (e.eventType == EventType.PACKET_END) {
            // close transmission
        }

        else if (e.eventType == EventType.JAMMING_START) {
            // remove the PacketStart associated with this jammed
        }

        else if (e.eventType == EventType.JAMMING_END) {
            // close transmission
        }

        else {
            System.out.println("Received unhandleable event");
            System.exit(1);
            return null;
        }
        */
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

        if (isInterrupt(e)) {
            return handleInterrupt();
        }
            
        else if (e.startsTransmission()) {
            openTransmissionFor(e);

            if (e.eventType == EventType.JAMMING_START) {
                
            }            
        } 

        else if (e.endsTransmission(e)) {
            closeTransmissionFor(e);
            // should try to send packet if we are eager AND it isnt just
            // a preamble_end (because we're immediately gonna receive packet)

        } 

        System.out.println("What else is there...?");
        System.exit(1);
        return null;
    }

    private boolean own(Event e) {
        return e.source == this;
    }

    private Action nextActionInSequence(Event e) {
        assert own(e);
        return null;
    }

    /* this will allow for creating distributions of sizes and such */
    private int nextPacketSize() {
        return 1; // todo: fix me
    }

    private void openTransmissionFor(Event e) {
        openTransmissions.add(e);
    }

    private void closeTransmissionFor(Event e) {

    }

    private Action handlePacketReady() {
        assert state == State.PREPARING_NEXT_PACKET;

        transitionTo(State.EAGER_TO_SEND); // below is going to fuck up state

        return isLineIdle() ? new Action(ActionType.SEND_PREAMBLE, Event.PREAMBLE_TIME, this) : null;
    }

    private boolean isLineIdle() {
        return openTransmissions.size() == 0;
    }

    private boolean isInterrupt(Event e) {
    	assert e.source != this;        
    	return e.startsTransmission() && isTransmitting() && !transmittingPreamble();
    }

    public boolean transmittingPreamble() {
    	return this.state == State.TRANSMITTING_PACKET_PREAMBLE;
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public Action handleInterrupt() {
        System.out.format("%d INTERRUPTED\n", id);

        System.out.println("need to implement something to invalidate the existing events");
        System.exit(1);

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
}
