package DBWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class NeedsABetterName
{
	private ArrayList<DataPoint> dataPoints;
	private LocalDateTime dateTime;
	
	private DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

	public void write() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.makeFileName()));
		
		writer.write(padRight("16", 16-1) + "\n");
		
		for (DataPoint dp : this.dataPoints) {
			writer.write(padRight(dp.clientID + "=" + dp.temp, 16-1) + "\n");
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
	
	public void setDataPoints(ArrayList<DataPoint> dps)
	{
		this.dataPoints = dps;
	}
	
	public void setDateTime(LocalDateTime dt)
	{
		this.dateTime = dt;
	}
}
