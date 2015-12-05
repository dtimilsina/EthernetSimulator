public enum ActionType {
	SEND_PREAMBLE, // => (all) PreambleStart, PreambleEnd
	SEND_PACKET,   // => (all) PacketStart, PacketEnd
	SEND_JAMMING,  // => (all) JammingStart, JammingEnd 
	BACKOFF        // => (us) BackoffEnd
	;
}