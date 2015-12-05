import java.util.*;

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
    public static double PROPAGATION_SPEED = 4.0; // distance units / time unit
    public static double TRANSMISSION_RATE = 10.0;


    public EventType eventType;
    
    public double time; // Time this event reaches dest machine
    
    public Node source; // Machine which generated the event
    public Node dest;   // Machine to which #time applies

    public int id;

    public boolean active = true; // Whether the event is still valid
    
    static Random rand = new Random(1);

    /*
    rework this to have a separate duration and tim setting so that
    we arent reassigning the time variable but rather setting the time
    based on the duration provided by the event itself, and set the time
    in the network class when it actually runs
    */

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
        return new Event(source, dest, time, eventType);
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

    public static Event JammingStart(Node source, double time) {
        return new Event(source, time, EventType.JAMMING_START);
    }

    public static Event JammingEnd(Node source, double time) {
        return new Event(source, time, EventType.JAMMING_END);
    }    

    public static double samplePacketReadyTime() {
        return Event.PACKET_READY_TIME + rand.nextGaussian();
    }

    public int compareTo(Event other) {
        return new Double(this.time).compareTo(new Double(other.time));//this.time >= other.time ? 0 : 1;
    }

    public boolean startsTransmission() {
        return this.eventType == EventType.PREAMBLE_START ||
               this.eventType == EventType.PACKET_START   ||
               this.eventType == EventType.JAMMING_START;
    }

    public boolean endsTransmission() {
        return this.eventType == EventType.PREAMBLE_END ||
               this.eventType == EventType.PACKET_END   ||
               this.eventType == EventType.JAMMING_END;
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

    public String toKey() {
        return source.id + ":" + dest.id + ":" + time + ":" +eventType;
    }

    public int hashCode() {
        return toKey().hashCode();
    }

    public boolean equals(Object obj) {
       if (!(obj instanceof Event))
            return false;
        if (obj == this)
            return true;
        return this.toKey() == ((Event) obj).toKey();
    }

    /** Testing **/
    public static void main(String[] args) {
        Event p = Event.PacketReady(null, 1);
        assert p.startsTransmission();

        Event ps = Event.PreambleStart(null, 1);
        assert !ps.startsTransmission();

        Event pe = Event.PreambleEnd(null, 1);
        assert !pe.startsTransmission();
    }
}

