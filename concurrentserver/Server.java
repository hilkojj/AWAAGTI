import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Server {
	public static final int PORT = 7789;
	private static final int maxnrofConnections = 800;

	private static int head = 0;
	private static int tail = 0;
	public static int lastTime;

	public static synchronized void updateTime(int time) {
		lastTime = time;
	}

	// public static Map<Integer, Float> data = new ConcurrentHashMap<Integer, Float>();
	public static int[] revStation = new int[8010];
	public static String[][] data = new String[8000][60];
	public static Map<Integer, Integer> stations = new ConcurrentHashMap<Integer, Integer>();
	public static VMDB db;
	public static FixedRingArray[] queues = new FixedRingArray[8010];

	public static void main(String[] args) {
		for (int i = 0; i < 8010; i++) {
			queues[i] = new FixedRingArray();
		}
		Socket connection;
		try {
			db = new VMDB("145.37.165.150", 9009);
			ServerSocket server = new ServerSocket(PORT);
			System.err.println("Server started with a maximum of: " + maxnrofConnections + " Connections");

			while (true) {
				connection = server.accept();
				System.err.println("New connection accepted. handing it over to worker thread");
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
