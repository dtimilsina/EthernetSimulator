public class Action {

	public ActionType actionType;

	public double duration;

	public Node source;

	public int packetId;

	private Action(ActionType actionType, double duration, Node source) {
		this.actionType = actionType;
		this.duration = duration;
		this.source = source;
	}

	public Action(ActionType actionType, double duration, Node source, int packetId) {
		this(actionType, duration, source);
		this.packetId = packetId;
	}

	public String toString() {
		return String.format("p%d %s m%d d%f", packetId, actionType.name(), source.id, duration);
	}

}

