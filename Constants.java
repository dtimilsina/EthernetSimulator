public class Constants {

	public final static double SLOT_TIME         = 512.0;

	public final static double INTERPACKET_GAP   = 96.0;

	public final static double PREAMBLE_TIME     = 64.0;

	public final static double JAMMING_TIME      = 32.0; // groovy

	public final static double PACKET_READY_TIME = 1024.0;

	public final static double PROPAGATION_SPEED = 60.0; // 6*10^8 ft/s in ft/bittime

	public final static double TRANSMISSION_RATE = 1.0;

	public final static int MAX_BACKOFF_TIMES = 16;
	public final static int MAX_BACKOFF_SLOTS = 1024;

	public static final int MIN_PACKET_SIZE = 512;
	public static       int MAX_PACKET_SIZE = 2048;
}