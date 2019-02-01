import java.net.*;
import java.io.*;
import java.util.*;

class WorkerThread implements Runnable {
	private Socket connection;

	private final static boolean SHOW_DEBUG = true;
	private final static boolean SHOW_ERROR = true;
	private final static boolean DATABASE = false;
	private static int num = 0;

	public boolean debug = false;

	public WorkerThread(Socket connection) {
		this.connection = connection;
	}

	public void run() {
		print("Worker thread started\n");

		try {
			String s;

			BufferedReader bin = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String[] input = new String[14];
			boolean fill = false;
			float temperatureSecond[] = new float[8010];
			float windSpeedSecond[] = new float[8010];

			Map<String, Integer> lookup = new HashMap<String, Integer>();
			lookup.put("STN", 0);
			lookup.put("DATE", 1);
			lookup.put("TIME", 2);
			lookup.put("TEMP", 3);
			lookup.put("DEWP", 4);
			lookup.put("STP", 5);
			lookup.put("SLP", 6);
			lookup.put("VISIB", 7);
			lookup.put("WDSP", 8);
			lookup.put("PRCP", 9);
			lookup.put("SNDP", 10);
			lookup.put("FRSHTT", 11);
			lookup.put("CLDC", 12);
			lookup.put("WNDDIR", 13);


			while ((s = bin.readLine()) != null) {
				if (s.equals("\t</MEASUREMENT>")) {
					int stationID = Integer.parseInt(input[0]);
					Integer mappedStationID = Server.stations.get(stationID);
					float temperature = Float.parseFloat(input[3]);
					float windSpeed = Float.parseFloat(input[8]);

					mapIncrement(stationID);
					if (mappedStationID != null) {
						if (!fill) {
							Server.temperatures[mappedStationID].put(temperature);
							Server.windSpeeds[mappedStationID].put(windSpeed);
							if(Server.temperatures[mappedStationID].hasBeanRound)
								fill = true;
						}
						else {
							float avg30temp = 0;
							float avg30windSpeed = 0;
							for (int i = 0; i < 30; i++) {//TODO: kan sneller!!!!
								avg30windSpeed += Server.windSpeeds[mappedStationID].get(i);
								avg30temp += Server.temperatures[mappedStationID].get(i);
							}
							avg30temp /= 30;
							avg30windSpeed /= 30;

							if (avg30windSpeed >= 1.2 * windSpeed || avg30windSpeed <= 0.8 * windSpeed) {
								Server.windSpeeds[mappedStationID].put(avg30windSpeed);
							} else {
								Server.windSpeeds[mappedStationID].put(windSpeed);
							}

							if (Math.abs(avg30temp) >= Math.abs(1.2 * temperature) || Math.abs(avg30temp) <= Math.abs(0.8 * temperature)) {
								Server.temperatures[mappedStationID].put(avg30temp);
							} else {
								Server.temperatures[mappedStationID].put(temperature);
							}
							int currTime = Integer.parseInt(input[2].replaceAll(":", ""));
							if (currTime > Server.lastTime && input[2] != null) {
								Server.updateTime(currTime);
								for (int i = 0; i < Server.stations.size(); i++) {
									windSpeedSecond[i] = Server.windSpeeds[i].get(0);
									temperatureSecond[i] = Server.temperatures[i].get(0);
								}
								Server.db.sendData(temperatureSecond, windSpeedSecond, Server.stations.size(), input[1], input[2]);
							}
						}
					}
					continue;
				}

				int start = 0; // TODO: we currently only parse xml that opens and closes a tag on the same line
				int end = 0;
				int lookup_index = 0;
				String value = "";

				int i = 0;
				for (; i < s.length(); i++) {
					if (s.charAt(i) == '>') {
						start = i;
					} else if (start != 0 && s.charAt(i) == '/') {
						value = s.substring(start + 1, i - 1);
						end = i;
						break;
					}
				}

				if (start != 0 && end != 0) {
					for (; i < s.length(); i++) {
						if (s.charAt(i) == '>') {
							try {
								lookup_index = lookup.get(s.substring(end + 1, i));
							} catch (NullPointerException e) {
								error("UNSUPPORTED TAG in XML INPUT: " + s.substring(end + 1, i));
								break;
							}

							if (end - start <= 2) {
								// print("EMPTY DATA FOR " + s.substring(end + 1, i));
							} else {
								input[lookup_index] = value;
							}
							break;
						}
					}
				}
			}

		} catch (ConnectException c) {
			print("DB connect error");
			c.printStackTrace();
		} catch (Exception e) {
			print("Connection closed unexpectedly");
			// System.err.println("Exception: " + Arrays.toString(e.getStackTrace()) + e.getMessage());
		}
	}

	private static void print(Object o) {
		if (SHOW_DEBUG)
			System.out.println(o);
	}

	private static void error(Object o) {
		if (SHOW_ERROR)
			System.err.println(o);
	}

	private static synchronized void mapIncrement(Integer station) {
		if (Server.stations.get(station) == null) {
			Server.stations.put(station, num);
			Server.revStation[num++] = station;
		}
	}
}

/*

   <!--Het weatherdata-element bevat meerdere measurement-elementen-->
   <WEATHERDATA>

   10 x  <MEASUREMENT>
   <STN>123456</STN>
   <DATE>2009-09-13</DATE>
   <TIME>15:59:46</TIME>
   <TEMP>-60.1</TEMP>
   <DEWP>-58.1</DEWP>
   <STP>1034.5</STP>
   <SLP>1007.6</SLP>
   <VISIB>123.7</VISIB>
   <WDSP>10.8</WDSP>
   <PRCP>11.28</PRCP>
   <SNDP>11.1</SNDP>
   <FRSHTT>010101</FRSHTT>
   <CLDC>87.4</CLDC>
   <WNDDIR>342</WNDDIR>
   </MEASUREMENT>

   </WEATHERDATA>

*/
