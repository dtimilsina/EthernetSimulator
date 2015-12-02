import java.util.*;

public class Main {

	public static void main(String[] args) {
		Collection<Node> nodes = new ArrayList<Node>();		
		EventQueue eq = new EventQueue(nodes);

		int[] positions = {1, 3, 5, 7}; // this should be some hash

		/*
		need to abstract away the topology. perhaps rename this
		to be liek a 'network' class which has the eventqueue,
		and takes over some of the functionality of eventqueue 
		(like the subevent splitting). 
		it should hold the topology (a hash from node to position)
		here, and use that to spawn subevents
		eventqueue should JUST be a wrapper really
		*/

		for (int i = 0; i < positions.length; i++) {
			Node node = new Node(i, positions[i]);
			nodes.add(node);
			assert node.state == State.PREPARING_NEXT_PACKET;
		}
	}
}