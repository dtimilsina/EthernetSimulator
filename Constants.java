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

	// Tc= 187.26
	public static final double[] IDLE_AVG_OPT = 
		{ 0.0, 0.165676424544, 0.217216624025, 0.243105350228, 
		0.259273180814, 0.271200922823, 0.277410441051, 0.280079249482, 
		0.285330401273, 0.288474425688, 0.289439372387, 0.294890010257, 
		0.295869979428, 0.297825984078, 0.298903609572, 0.30049266655, 
		0.301739319221, 0.30255149031, 0.304626620878, 0.304092598516, 
		0.304729701052, 0.305366928664, 0.306037719494, 0.306535179303 };

	// Tc= 93.63
	public static final double[] IDLE_AVG_OPT_HALF =
		{ 0.0, 0.269875133178, 0.344828838757, 0.380880988642, 
		  0.401909793379, 0.416263464468, 0.426124189927, 0.433267290928, 
		  0.439529243674, 0.44361052477, 0.447677885388, 0.450544608856, 
		  0.453205445799, 0.45549723155, 0.457376496252, 0.459094928385, 
		  0.460644034951, 0.461932333559, 0.463227471457, 0.464186231568, 
		  0.465303185301, 0.466032607199, 0.466819532925, 0.467588423275 };

	// Idle sense
	public static int MAX_TRANS = 10;
	public static double IDLE_TARGET = 0;

	public static 		int EPS = 3;
	public static 		double ALPHA = 0.95;
	public static 		double BETA = .8;
	public static 		int GAMMA = 1;
}