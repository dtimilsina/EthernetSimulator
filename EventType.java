public enum EventType {
	PACKET_READY,
	PREAMBLE_START,
	PREAMBLE_END,
	PACKET_START,
	PACKET_END,
	JAMMING_START,
	JAMMING_END,
	BACKOFF_END,
	WAIT_END;
}


/*

Action a preamble => preamble start, end, packet start, end

*/