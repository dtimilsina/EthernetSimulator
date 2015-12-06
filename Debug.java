public class Debug {

	public static int threshold = 0;

	public static void print(String s, int priority) {
		if (priority >= Debug.threshold) {
			System.out.println(s);
		}
	}
}