import java.util.*;

public class Event implements Comparable<Event> {

    // move these. maybe move things in terms of slot_times only?
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


    public Event(EventType type, Node source, Node dest, double time) {
        this.eventType = type;
        this.source = source;
        this.dest = dest;
        this.time = time;        
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

    public boolean doesTransmit() {
        return this.eventType == EventType.PREAMBLE_START ||
               this.eventType == EventType.PACKET_START   || 
               this.eventType == EventType.JAMMING_START;
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

    }
}

