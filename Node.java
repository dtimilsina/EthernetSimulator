import java.util.*;

public class Node {

	public int id;
	public State state = State.UNINITIALIZED;

    public Collection<Event> openTransmissions;


    public Node(int id) {
		this.id = id;
    }

    public Event start() {
        transitionTo(State.PREPARING_NEXT_PACKET);
        
        return Event.PacketReady(this);
    }

    public Event react(Event e) {
    	assert e.dest == this;

        if (e.eventType == EventType.PACKET_READY) {
            System.out.println("READY");
            System.exit(0);
        }

    	else if (isInterrupt(e)) {
    		transmitJammingSignal();
    	}

    	assert false;
    	return null;
    }

    public boolean isInterrupt(Event e) {
    	assert e.source != this;
    	return e.doesSendBits() && isTransmitting() && !transmittingPreamble();
    }

    public boolean transmittingPreamble() {
    	return this.state == State.TRANSMITTING_PACKET_PREAMBLE;
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public Event transmitJammingSignal() {
    	transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

    	assert false;
    	return null;
    }

    public void transitionTo(State newState) {
    	assert this.state != newState;

    	this.state = newState;
    }

    public String toString() {
        return String.format("id:%d state:%s", id, state.name());
    }
}
