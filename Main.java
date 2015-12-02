import java.util.*;

public class Main {

	public static void main(String[] args) {
		Collection<Node> nodes = new ArrayList<Node>();		
		EventQueue eq = new EventQueue(nodes);
		int[] positions = {1, 3, 5, 7};
		for (int i = 0; i < positions.length; i++) {
			Node node = new Node(i, positions[i], eq);
			nodes.add(node);
			assert node.state == State.PREPARING_NEXT_PACKET;
		}


	}

}