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
	public static final double[] nIdleAvgOpt = 	
	{ 1.0, 1.16567642454, 1.21721662402, 1.24310535023, 1.25927318081, 1.27120092282, 
	1.27741044105, 1.28007924948, 1.28533040127, 1.28847442569, 1.28943937239, 1.29489001026, 
	1.29586997943, 1.29782598408, 1.29890360957, 1.30049266655, 1.30173931922, 1.30255149031, 
	1.30462662088, 1.30409259852, 1.30472970105, 1.30536692866, 1.30603771949, 1.3065351793 };


	// Tc= 93.63
	public static final double[] nIdleAvgOptHalf =
	{ 1.0, 1.26987513318, 1.34482883876, 1.38088098864, 1.40190979338, 1.41626346447, 
	1.42612418993, 1.43326729093, 1.43952924367, 1.44361052477, 1.44767788539, 1.45054460886, 
	1.4532054458, 1.45549723155, 1.45737649625, 1.45909492839, 1.46064403495, 1.46193233356, 
	1.46322747146, 1.46418623157, 1.4653031853, 1.4660326072, 1.46681953292, 1.46758842328};

	// Idle sense
	public static int MAX_TRANS = 10;
	public static double nIdleTarget = 0;

	public static 		int EPS = 3;
	public static 		double ALPHA = 0.95;
	public static 		double BETA = .8;
	public static 		int GAMMA = 1;
}