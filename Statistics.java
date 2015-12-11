/*
 * this will eventually house the code that stores and
 * writes out to csv the data we've collected
 */

public class Statistics {

	public int bitsSent;
	public int successfulPackets = 0;
	public int collisions = 0;
	public int slotsWaited = 0;
	public int packetsAborted = 0;

	// Idle sense
	/*
	public int idleSlots = 0;
	public int numTransmissions = 0;

	public void addIdleSlots(int n) {
		idleSlots += n;
	}

	public void incrTransmissions() {
		numTransmissions++;
	}
	*/

	public void addSuccessfulPacket(int sentBits) {
		successfulPackets++;
		bitsSent += sentBits;
	}

	public void addCollision() {
		collisions++;
	}

	public void addSlotsWaited(int slots) {
		slotsWaited += slots;
	}

	public double computeDumbEfficiency(){
		return (bitsSent * 1) / (slotsWaited * Constants.SLOT_TIME);
	}

	public void addAbort(){
		packetsAborted++;
	}
}