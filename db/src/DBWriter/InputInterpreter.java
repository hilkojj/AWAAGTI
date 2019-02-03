package DBWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

import shared.DBFile;
import shared.DataPoint;
import shared.Logger;
import shared.Settings;

/**
 * InputInterpreter interprets the stream of data from the weather
 * station machine. It stores the weather data from the stream, to the
 * database.
 * 
 * This class is a semi state machine, as in, it keeps track of what
 * part of the stream is expected and interprets the bytes accordantly.
 * 
 * @author remi
 *
 */
public class InputInterpreter
{
	
	private ArrayList<DataPoint> list;
			
	private static final DateTimeFormatter dateTimeFormatter =
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
		if (line.contentEquals("END")) {
			this.stateFinish();
		} else if (line.contentEquals("START")) {
			if (this.state == 1) {
				// Treat START as END when not ENDed yet, to keep it
				// backwards compatible with Pi clients not sending an "END".
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
			System.out.println("InputInterpreter: invalid header parts length");
			return;
		}
		
		dateTime = LocalDateTime.parse(parts[0] + " " + parts[1],
				dateTimeFormatter);

		state = 1;
	}
	
	private void stateDataPoint(String line)
	{

		String[] parts = line.split(",");
		if (parts.length < 2) {
			System.out.println("InputInterpreter: invalid datapoint parts length");
			return;
		}

		DataPoint dp = new DataPoint();
		dp.setClientID(Integer.parseInt(parts[0]));
		float temp =  Float.parseFloat(parts[1]);
		dp.setTemp((int) (temp*10));
		
		if (parts.length >= 3) {
			float wind =  Float.parseFloat(parts[2]);
			dp.setWindSpeed((int) (wind*10));
		}
		
		this.list.add(dp);
	}
	
	private void stateFinish()
	{
		try {
			Collections.sort(this.list);
			
			long uts = this.dateTime.toEpochSecond(ZoneOffset.UTC);
			String dir = DBFile.dirForUTS(uts);

			File directory = new File(dir);
		    if (!directory.exists()){
		    	System.out.println("Make dir");
		        directory.mkdirs();
		    }

			DBFile dbFile = new DBFile();
			dbFile.setFileName(dir + uts + "."+Settings.DATA_EXTENSION);
			dbFile.setDataPoints(this.list);
			dbFile.write();
		} catch (IOException e) {
			Logger.log("ERROR: cannot write to database file: " + e.getMessage());
		}
		this.state = 0;
	}
}
