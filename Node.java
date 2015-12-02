public class Node {

	public int id;
	public State state;
	public int x; // For relative positioning within the network

	// I think it'll be nice to be able to have each machine
	// add to the eventqueue directly when it reacts to some event
	public EventQueue eventQueue;

    public Node(int id, int x, EventQueue eq) {
		this.id = id;
		this.x = x;
		this.eventQueue = eq;		
    }

    public void start() {
    	transitionTo(State.PREPARING_NEXT_PACKET);
    }

    public void react(Event e) {
    	assert e.dest == this;

    	if (isInterrupt(e)) {
    		transmitJammingSignal();
    	}
    }

    public boolean isInterrupt(Event e) {
    	assert e.source != this;
    	return e.doesSendBits() && isTransmitting() && !transmittingPreamble();
    }

    public boolean transmittingPreamble() {
    	return this.state == State.TRANSMITTING_PACKET_PREAMBLE:
    }

    public boolean isTransmitting() {
    	return this.state.isTransmittingState();
    }

    public void transmitJammingSignal() {
    	transitionTo(State.TRANSMITTING_JAMMING_SIGNAL);

    	

    	assert false;
    }

    public void transitionTo(State newState) {
    	assert this.state != newState;

    	this.state = newState;

    }
}
