import java.util.*;

public class Event implements Comparable<Event> {

    // move these. maybe move things in terms of slot_times only?
    public static double SLOT_TIME = 512.0;
    public static double INTERPACKET_GAP = 96.0; // todo: check
    public static double PREAMBLE_TIME = 64.0;
    public static double JAMMING_TIME = 32.0;
    public static double PACKET_READY_TIME = 100.0;//SLOT_TIME / 2;

    // distance units / time unit
    public static double PROPAGATION_SPEED = 60.0; // 6*10^8 ft/s in ft/bittime

    public EventType eventType;
    
    public double time; // Time this event reaches dest machine
    
    public Node source; // Machine which generated the event
    public Node dest;   // Machine to which #time applies

    public int id;
    public int packetId = -1;

    public boolean active = true; // Whether the event is still valid
    
    static Random rand = new Random(1);


    public Event(EventType type, Node source, Node dest, double time) {
        this.eventType = type;
        this.source = source;
        this.dest = dest;
        this.time = time;        
    }

    public Event(EventType type, Node source, Node dest, double time, int packetId) {
        this(type, source, dest, time);
        this.packetId = packetId;
    }

    public static Event PacketReady(Node source) {
        return new Event(EventType.PACKET_READY, source, source, Event.samplePacketReadyTime());
    }

    public static double samplePacketReadyTime() {
        return Event.PACKET_READY_TIME + rand.nextGaussian();
    }

    public int compareTo(Event other) {
        return new Double(this.time).compareTo(new Double(other.time));//this.time >= other.time ? 0 : 1;
    }

    public boolean isStartEvent() {
        return this.eventType == EventType.PREAMBLE_START ||
               this.eventType == EventType.PACKET_START   || 
               this.eventType == EventType.JAMMING_START;
    }

    public String toString() {
        String destId = "x";
        if (dest != null) { destId = "" + dest.id; }
        return String.format("pId:%d t%f m%s -> m%s %s %f", packetId, time, source.id, destId, eventType.name(), time);
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

    }
}

