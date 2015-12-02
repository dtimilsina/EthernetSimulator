import java.util.Random;

public class Event implements Comparable<Event> {
    double time; // Time this event reaches dest machine
    Node source; // Machine which generated the event
    Node dest;   // Machine to which #time applies
    static Random rand = new Random(1);

    /* Don't invoke this directly */
    public Event(double time, Node source, Node dest) {
        this.time = time;
        this.source = source;
        this.dest = dest;
    }

    public int compareTo(Event other) {
        return this.time >= other.time ? 1 : 0;
    }
}

class PacketReady extends Event {
    public static double sampleTime() {
        return rand.nextGaussian() + 2.0; // gaussian around 2.0
    }

    public PacketReady(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}
/*
class PreambleStart extends Event {

}

class PreambleEnd extends Event {

}
*/