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
    	// enqueue the packet ready event
    }

    public void react(Event e) {
    	assert e.dest == this;
    }

    public void transitionTo(State newState) {
    	assert this.state != newState;

    	this.state = newState;
    }
}
