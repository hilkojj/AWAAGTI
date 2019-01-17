package DBWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * OutputWriter structures the DataPoints in the structures DB file
 * format, and writes to file to the filesystem.
 * 
 * @author remi
 *
 */
public class OutputWriter
{
	private ArrayList<DataPoint> dataPoints;
	private LocalDateTime dateTime;
	
	private DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	/**
	 * Writer formats the DataPoint data and writes the file to the filesystem.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.makeFileName()));
		
		// Determine required line length
		int highest = 0;
		int length;
		for (DataPoint dp : this.dataPoints) {
			length = dp.makeDBLine().length();
			if (length > highest) {
				highest = length;
			}
		}
		
		writer.write(padRight(highest+1 + "", highest) + "\n");
		// (highest+1, because the linebreak is not included in 'highest'
		//   but is part of the line length of course)
		
		for (DataPoint dp : this.dataPoints) {
			writer.write(padRight(dp.makeDBLine(), highest) + "\n");
		}
		 
		writer.close();
	}
	
	private String makeFileName()
	{
		return this.dateTime.format(this.dateTimeFormatter) + ".txt";
	}
	
	private static String padRight(String s, int n)
	{
	     return String.format("%1$-" + n + "s", s).replace(' ', '#'); 
	}
	
	/**
	 * setDataPoints
	 * 
	 * @param dps
	 */
	public void setDataPoints(ArrayList<DataPoint> dps)
	{
		this.dataPoints = dps;
	}
	
	/**
	 * setDateTime
	 * 
	 * @param dt
	 */
	public void setDateTime(LocalDateTime dt)
	{
		this.dateTime = dt;
	}
}
