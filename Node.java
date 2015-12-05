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

        if (e.eventType == EventType.PACKET_READY) {
            return handlePacketReady();
        } 

        else if (isInterrupt(e)) {
            return handleInterrupt();
        } 

        else {
            System.out.println("Received unhandleable event");
            System.exit(1);
            return null;
        }
    }

    // public Event xxxx_react(Event e) {
    // 	assert e.dest == this;

    //     if (e.source == this) {
    //         return handleOwnEvent(e); // for clocking the xEnd 
    //     } 

    //     else if (isInterrupt(e)) {
    // 		return handleInterrupt();
    // 	}

    //     // I'm receiving bits, but I'm not currently sending anything
    //     // else if (e.startsTransmission()) {
    //     //     openTransmissionFor(e);
    //     // }

    //     // else if (e.endsTransmission()) {
    //     //     closeTransmissionFor(e);
    //     // }

    //     /* if opens new transmission, need to add it to openTransmissions */

    //     /* else if ends transmission and im eager to send */
    //     else {
    //         System.out.format("#react cannot handle: %s\n", e);
    //     	assert false;
    //     	return null;
    //     }
    // }

    private void openTransmissionFor(Event e) {
        openTransmissions.add(e);
    }

    private void closeTransmissionFor(Event e) {

    }

    /* For doing things like clocking PREAMBLE_START->END
    * as well as the usual, like PACKET_READY
    */
    // private Event handleOwnEvent(Event e) {
    //     assert e.source == this;

    //     if (e.eventType == EventType.PACKET_READY) {
    //         return handlePacketReady();
    //     } 

    //     else if (e.eventType == EventType.PREAMBLE_START) {
    //         return Event.PreambleEnd(this, Event.PREAMBLE_TIME);
    //     } 

    //     else {
    //         System.out.format("Received unprocessable event: %s\n", e);
    //         assert false;
    //         return null;
    //     }
    // }

    private Action handlePacketReady() {
        assert state == State.PREPARING_NEXT_PACKET;

        transitionTo(State.EAGER_TO_SEND);

        return isLineIdle() ? new Action(ActionType.SEND_PREAMBLE, Event.PREAMBLE_TIME, this) : null;
    }

    // private Event handlePacketReady() {
    //     assert state == State.PREPARING_NEXT_PACKET;

    //     transitionTo(State.EAGER_TO_SEND);

    //     return isLineIdle() ? Event.PreambleStart(this, 0.0) : null;
    // }

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

        transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

        return new Action(ActionType.SEND_JAMMING, Event.JAMMING_TIME, this);
    }

    // public Event handleInterrupt() {
    //     System.out.format("%d INTERRUPTED\n", id);

    // 	transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

    // 	return Event.JammingStart(this, 0.0);
    // }

    public void transitionTo(State newState) {
    	assert this.state != newState;

    	this.state = newState;
    }

    public String toString() {
        return String.format("id:%d state:%s", id, state.name());
    }
}
