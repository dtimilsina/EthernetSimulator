import java.util.*;

public class Event {

    public static double SLOT_TIME = 512.0;
    public static double INTERPACKET_GAP = 96.0; // todo: check
    public static double PREAMBLE_TIME = 64.0;
    public static double JAMMING_TIME = 32.0;
    public static double PACKET_READY_TIME = 1024.0;

    // distance units / time unit
    public static double PROPAGATION_SPEED = 60.0; // 6*10^8 ft/s in ft/bittime

    public EventType eventType;
    
    public double time; // Time this event reaches dest machine
    
    public Node source; // Machine which generated the event
    public Node dest;   // Machine to which #time applies

    public int id;
    public Integer packetId;

    public boolean active = true; // Whether the event is still valid
    
    static Random rand = new Random(1);

    public Event(EventType type, Node source, Node dest, double time, int packetId) {
        this.eventType = type;
        this.source = source;
        this.dest = dest;
        this.time = time;        
        this.packetId = packetId;
    }

    public static double samplePacketReadyTime() {
        return Event.PACKET_READY_TIME + rand.nextGaussian();
    }

    public boolean isStartEvent() {
        return this.eventType == EventType.PREAMBLE_START ||
               this.eventType == EventType.PACKET_START   || 
               this.eventType == EventType.JAMMING_START;
    }

    public boolean isEndEvent() {
        return this.eventType == EventType.PREAMBLE_END ||
               this.eventType == EventType.PACKET_END ||
               this.eventType == EventType.JAMMING_END;
    }

    public String toString() {
        String destId = "x";
        if (dest != null) { destId = "" + dest.id; }
        return String.format("p%d t%f m%s -> m%s %s %f", packetId, time, source.id, destId, eventType.name(), time);
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
        EventQueue q = new EventQueue();
        Event start = new Event(EventType.PREAMBLE_END, new Node(1), new Node(2), 0.0, 0);
        Event end = new Event(EventType.JAMMING_START, new Node(3), new Node(4), 0.0, 0);
        System.out.println(start);
        System.out.println(end);
        q.add(start);
        q.add(end);
        assert q.next() == start;
        assert q.next() == end;
        q.add(end);
        q.add(start);
        assert q.next() == start;
    }
}

