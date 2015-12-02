public class Node {

	public int id;
	public State state;

    public Node(int id) {
		this.id = id;
    }

    public void start() {
    	transitionTo(State.PREPARING_NEXT_PACKET);
    }

    public Event react(Event e) {
    	assert e.dest == this;

    	if (isInterrupt(e)) {
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
}
