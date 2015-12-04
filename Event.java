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
    public static double PACKET_READY_TIME = 2.0;


    public EventType eventType;
    
    public double time; // Time this event reaches dest machine
    
    public Node source; // Machine which generated the event
    public Node dest;   // Machine to which #time applies
    
    public boolean active = true; // Whether the event is still valid
    
    static Random rand = new Random(1);


    private Event(Node source, double time, EventType type) {
        this.source = source;
        this.time = time;        
        this.eventType = type;
    }

    private Event(Node source, Node dest, double time, EventType type) {
        this.source = source;
        this.dest = dest;
        this.time = time;        
        this.eventType = type;
    }

    public Event copy() {
        return new Event(source, time, eventType);
    }


    public static Event PacketReady(Node source, double time) {
        return new Event(source, source, time, EventType.PACKET_READY);
    }

    /* This will auto set some amount of time for the event */
    public static Event PacketReady(Node source) {
        return Event.PacketReady(source, Event.samplePacketReadyTime());
    }

    public static Event PreambleStart(Node source, double time) {
        return new Event(source, time, EventType.PREAMBLE_START);
    }

    public static Event PreambleEnd(Node source, double time) {
        return new Event(source, time, EventType.PREAMBLE_END);
    }

    // factory for each 
    //     PACKET_READY,
    // PREAMBLE_START,
    // PREAMBLE_END,
    // PACKET_START,
    // PACKET_END,
    // JAMMING_START,
    // JAMMING_END;    

    public static double samplePacketReadyTime() {
        return Event.PACKET_READY_TIME + rand.nextGaussian();
    }

    public int compareTo(Event other) {
        return this.time >= other.time ? 1 : 0;
    }

    public boolean doesSendBits() {
        return this.eventType == EventType.PREAMBLE_START ||
               this.eventType == EventType.PREAMBLE_END   ||
               this.eventType == EventType.PACKET_START   ||
               this.eventType == EventType.JAMMING_START;
    }

    public boolean doesConcernOthers() {
        System.out.println("NEED TO FINISH"); // todo: here here here
        return eventType != EventType.PACKET_READY;
    }

    public String toString() {
        String destId = "x";
        if (dest != null) { destId = "" + dest.id; }
        return String.format("t%f m%s -> m%s %s", time, source.id, destId, eventType.name());
    }

    /** Testing **/
    public static void main(String[] args) {
        Event p = Event.PacketReady(null, 1);
        assert p.doesSendBits();

        Event ps = Event.PreambleStart(null, 1);
        assert !ps.doesSendBits();

        Event pe = Event.PreambleEnd(null, 1);
        assert !pe.doesSendBits();
    }
}

