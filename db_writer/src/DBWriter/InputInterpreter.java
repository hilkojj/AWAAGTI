package DBWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * InputInterpreter interprets the stream of data from the weather
 * station machine.
 * This class is a semi state machine, as in, it keeps track of what
 * part of the stream is expected and interprets the bytes accordantly.
 * 
 * @author remi
 *
 */
public class InputInterpreter
{
	
	private ArrayList<DataPoint> list;
			
	private DateTimeFormatter dateTimeFormatter =
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private LocalDateTime dateTime;
	
	
	private int state;

	/**
	 * interpretLine interprets a single line of the input stream and
	 * uses the state of this class to determine how to interpret it.
	 * 
	 * @param line
	 */
	public void interpretLine(String line)
	{
		if (line.contentEquals("START")) {
			if (this.state == 1) {
				this.stateFinish();
			} else {
				this.stateInit();
			}
		} else if (state == 0) {
			this.stateHeader(line);
		} else if (state == 1) {
			this.stateDataPoint(line);
		}
	}
	
	private void stateInit()
	{
		System.out.println("InputInterpreter: init");
		this.list = new ArrayList<DataPoint>();
		this.dateTime = null;
		this.state = 0;
	}
	
	private void stateHeader(String line)
	{
		System.out.println("InputInterpreter: read header");

		// The header, with date and time information.
		String[] parts = line.split(",");
		if (parts.length < 2) {
			System.out.println("InputInterpreter: invalid header parts length");
			return;
		}
		
		dateTime = LocalDateTime.parse(parts[0] + " " + parts[1],
				dateTimeFormatter);
		System.out.println(dateTime);

		state = 1;
	}
	
	private void stateDataPoint(String line)
	{
		System.out.println("InputInterpreter: read datapoint");

		String[] parts = line.split("=");
		System.out.println("Parts:");
		System.out.println(parts);
		if (parts.length < 2) {
			System.out.println("InputInterpreter: invalid datapoint parts length");
			return;
		}

		DataPoint dp = new DataPoint();
		dp.clientID = Integer.parseInt(parts[0]);
		float temp =  Float.parseFloat(parts[1]);
		dp.temp = (int) (temp*10);
		
		list.add(dp);
	}
	
	private void stateFinish()
	{
		System.out.println("InputInterpreter: finish!");
		try {
			Collections.sort(this.list);
			OutputWriter stuffs = new OutputWriter();
			stuffs.setDateTime(this.dateTime);
			stuffs.setDataPoints(this.list);
			stuffs.write();
		} catch (IOException e) {
			System.out.println("ERROR: cannot write to database file");
			System.out.println(e);
		}
		this.state = 0;
	}
}