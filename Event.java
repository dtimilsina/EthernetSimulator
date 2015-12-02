import java.util.Random;

public class Event implements Comparable<Event> {

    public static double SLOT_TIME = 512.0;
    public static double INTERPACKET_GAP = 0.96; // todo: check
    public static double PREAMBLE_TIME = 64.0;
    public static double JAMMING_TIME = 32.0;

    double time; // Time this event reaches dest machine
    Node source; // Machine which generated the event
    Node dest;   // Machine to which #time applies
    boolean active = true; // Whether the event is still valid
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

class PacketReady extends Event {
    public static double sampleTime() {
        return rand.nextGaussian() + 2.0; // gaussian around 2.0
    }

    public PacketReady(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}

class PreambleStart extends Event {
    public PreambleStart(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}

class PreambleEnd extends Event {
    public PreambleEnd(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}

class PacketStart extends Event {
    public PacketStart(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}

class JammingStart extends Event {
    public JammingStart(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}

class JammingEnd extends Event {
    public JammingEnd(double time, Node source, Node dest) {
        super(time, source, dest);
    }
}
