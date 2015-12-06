public enum ActionType {
	WAIT,          // => (us)  
	BACKOFF,       // => (us)
	PREPARE_PACKET, // => (us)
 	SEND_PREAMBLE, // => (all) PreambleStart, PreambleEnd
	SEND_PACKET,   // => (all) PacketStart, PacketEnd
	SEND_JAMMING,  // => (all) JammingStart, JammingEnd 
	;
}