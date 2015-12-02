public class Main {

	public static void main(String[] args) {
		EventQueue eq = new EventQueue();

		int[] positions = {1, 3, 5, 7};
		
		Node[] nodes = new Node[positions.length];
		
		for (int i = 0; i < positions.length; i++) {
			nodes[i] = new Node(i, positions[i], eq);
			assert nodes[i].state == State.PREPARING_NEXT_PACKET;
		}
		
		
	}

}