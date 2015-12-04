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

    public Event react(Event e) {
    	assert e.dest == this;

        /* For doing things like clocking PREAMBLE_START->END
         * as well as the usual, like PACKET_READY
         */
        if (e.source == this) {
            return handleOwnEvent(e);
        } 

        else if (isInterrupt(e)) {
    		return handleInterrupt();
    	}

        /* if opens new transmission, need to add it to openTransmissions */

        /* else if ends transmission and im eager to send */

    	assert false;
    	return null;
    }

    private Event handleOwnEvent(Event e) {
        assert e.source == this;

        if (e.eventType == EventType.PACKET_READY) {
            return handlePacketReady();
        } else if (e.eventType == EventType.PREAMBLE_START) {
            return Event.PreambleEnd(this, 0.0);
        } else {
            assert false;
            return null;
        }
    }

    private Event handlePacketReady() {
        assert state == State.PREPARING_NEXT_PACKET;

        transitionTo(State.EAGER_TO_SEND);

        return isLineIdle() ? Event.PreambleStart(this, 0.0) : null;
    }

    private boolean isLineIdle() {
        return openTransmissions.size() == 0;
    }

    private boolean isInterrupt(Event e) {
    	assert e.source != this;
    	return e.doesSendBits() && isTransmitting() && !transmittingPreamble();
    }

    public boolean transmittingPreamble() {
    	return this.state == State.TRANSMITTING_PACKET_PREAMBLE;
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public Event handleInterrupt() {
    	transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

    	return Event.JammingStart(this, 0.0);
    }

    public void transitionTo(State newState) {
    	assert this.state != newState;

    	this.state = newState;
    }

    public String toString() {
        return String.format("id:%d state:%s", id, state.name());
    }
}
