import java.io.PrintWriter;
import java.net.Socket;

/**
 * VMDB facilitates the communication with the database.
 */
public class VMDB {
	private Socket socket;
	private PrintWriter output;

	public VMDB(String host, int port) throws Exception {
		this.socket = new Socket(host, port);
		this.output = new PrintWriter(this.socket.getOutputStream());
	}

	/**
	 * sendBegin notifies the database that the start of a stream of datapoints is beginning.
	 * The database will save all of the following sendDataPoint calls together in one file.
	 *
	 * @param date in the format: 2006-01-02
	 * @param time in the format: 15:03:04
	 */
	private synchronized void sendBegin(String date, String time) {
		this.output.write("START\n");
		this.output.write(date + "," + time + "\n");
	}

	/**
	 * sendDataPoint
	 */
	private synchronized void sendDataPoint(int station, float temp) {
		this.output.format("%d,%.01f\n", station, temp);
	}

	public synchronized void sendData(float[] temperatures, float[] windSpeeds, int len, String date, String time) {
		sendBegin(date, time);
		for (int i = 0; i < len; i++) {
			output.format("%d,%.01f,%.01f\n", Server.revStation[i], temperatures[i], windSpeeds[i]);
		}
		sendEnd();
	}

	/**
	 * sendEnd notifies the database that no more dataPoints are coming
	 * for this second, so that the database can write the previously
	 * send datapoints to disk.
	 */
	private synchronized void sendEnd() {
		this.output.write("END\n");
		this.output.flush();
	}
}
