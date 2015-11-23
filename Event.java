/**
 * Event Class
 */

public class Event {

    double time;
    
    public Event(double time){
	this.time = time;
    }
    
}


/**
 * States for Ethernet Transmitter
 */
public enum TransmittedEventType {
    EAGER_TO_SEND,
    PREPARING_NEXT_PACKET,
    WAITING_FOR_BACK_OFF,
    TRANSMITTING_PACKET_PREAMBLE,
    TRANSMITTING_PACKET_CONTENTS,
    TRANSMITTING_JAMMING_SIGNAL
}

/**
 * States for Ethernet Receiver
 */
public enum ReceivingEventType{
    RECEIVER_BUSY,
    WAITING_INTER_PACKET_GAP,
    RECEIVER_IDLE
}
