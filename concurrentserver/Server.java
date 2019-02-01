import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Server {
	public static final int PORT = 7789;

	private static int head = 0;
	private static int tail = 0;
	public static int lastTime;

	public static synchronized void updateTime(int time) {
		lastTime = time;
	}

	public static int[] revStation = new int[8010];
	public static Map<Integer, Integer> stations = new ConcurrentHashMap<Integer, Integer>();
	public static VMDB db;
	public static FixedRingArray[] temperatures = new FixedRingArray[8010];
	public static FixedRingArray[] windSpeeds = new FixedRingArray[8010];

	public static void main(String[] args) {
		for (int i = 0; i < 8010; i++) {
			windSpeeds[i] = new FixedRingArray();
			temperatures[i] = new FixedRingArray();
		}
		Socket connection;
		try {
			db = new VMDB("145.37.165.150", 9009);
			ServerSocket server = new ServerSocket(PORT);
			System.err.println("Server started");

			while (true) {
				connection = server.accept();
				System.err.println("New connection accepted. Handing it over to worker thread");
				Thread worker = new Thread(new WorkerThread(connection));
				worker.start();
			}
		} catch (java.io.IOException ioe) {
			System.err.print("\n\nIOException\n\n");
		} catch (Exception e) {
			System.err.print("vmdb error\n");
		}
	}
}
