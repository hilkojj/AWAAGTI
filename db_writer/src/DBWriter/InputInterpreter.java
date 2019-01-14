package DBWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class InputInterpreter
{
	
	private ArrayList<DataPoint> list;
			
	private DateTimeFormatter dateTimeFormatter =
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private LocalDateTime dateTime;
	
	
	private int state;

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
		this.list = new ArrayList<DataPoint>();
		this.dateTime = null;
		this.state = 0;
	}
	
	private void stateHeader(String line)
	{
		// The header, with date and time information.
		String[] parts = line.split(",");
		if (parts.length < 2) {
			System.out.println("connection: invalid header parts length");
			return;
		}
		
		dateTime = LocalDateTime.parse(parts[0] + " " + parts[1],
				dateTimeFormatter);
		System.out.println(dateTime);

		state = 1;
	}
	
	private void stateDataPoint(String line)
	{
		String[] parts = line.split("=");
		System.out.println("Parts:");
		System.out.println(parts);
		if (parts.length < 2) {
			System.out.println("connection: invalid datapoint parts length");
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
		try {
			Collections.sort(this.list);
			NeedsABetterName stuffs = new NeedsABetterName();
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