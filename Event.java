import java.util.Random;

/*
-PreambleStart
-PreambleEnd
-PacketArrive
save event
PacketEnd
remove corresponding event
Interpacket gap
-JammingArrive/Start
save event
remove packet events associated with the packet being jammed
JammingEnd/Complete
remove corresponding event
Backoff waittime over
*/

public class Event implements Comparable<Event> {

    // move these
    public static double SLOT_TIME = 512.0;
    public static double INTERPACKET_GAP = 0.96; // todo: check
    public static double PREAMBLE_TIME = 64.0;
    public static double JAMMING_TIME = 32.0;


    public EventType eventType;
    
    public double time; // Time this event reaches dest machine
    
    public Node source; // Machine which generated the event
    public Node dest;   // Machine to which #time applies
    
    public boolean active = true; // Whether the event is still valid
    
    static Random rand = new Random(1);


    private Event(double time, Node source, EventType type) {
        this.time = time;
        this.source = source;
        this.eventType = type;
    }

    public Event copy() {
        return new Event(time, source, eventType);
    }

    public static Event PacketReadyEvent(double time, Node source) {
        return new Event(time, source, EventType.PACKET_READY);
    }

    // factory for each 
    //     PACKET_READY,
    // PREAMBLE_START,
    // PREAMBLE_END,
    // PACKET_START,
    // PACKET_END,
    // JAMMING_START,
    // JAMMING_END;    

    public int compareTo(Event other) {
        return this.time >= other.time ? 1 : 0;
    }

    public boolean doesSendBits() {
        return this instanceof PreambleStart ||
               this instanceof PreambleEnd ||
               this instanceof PacketStart ||
               this instanceof JammingStart;
    }

    /** Testing **/
    public static void main(String[] args) {
        PacketReady p = new PacketReady(1, null, null);
        assert p.doesSendBits();

        PreambleStart ps = new PreambleStart(1, null, null);
        assert !ps.doesSendBits();

        PreambleEnd pe = new PreambleEnd(1, null, null);
        assert !pe.doesSendBits();
    }
}

