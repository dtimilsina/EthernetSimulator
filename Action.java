public class Action {

	public ActionType actionType;

	public double duration;

	public Node source;

	public int packetId;

	public Action(ActionType actionType, double duration, Node source) {
		this.actionType = actionType;
		this.duration = duration;
		this.source = source;
	}

	public Action(ActionType actionType, double duration, Node source, int packetId) {
		this(actionType, duration, source);
		this.packetId = packetId;
	}

}

/*
Action a; (PREAMBLE) => EVENT[PREAMBLE_START] & EVENT[PREAMBLE_END]

for each  node:
	for start and end
	Event start;
	start.time = currentTime + propTime;
	start.source = a.source;
	start.dest = node;

	Event end;
	end.time = current  + a.duration + propTime;


*/
