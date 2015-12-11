import java.util.*;

public class Event {

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

