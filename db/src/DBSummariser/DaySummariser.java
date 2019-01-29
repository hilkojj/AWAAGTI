package DBSummariser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import shared.DBFile;
import shared.DataPoint;

public class DaySummariser extends Summariser
{
	private int year, month, day, hour;
	
	public DaySummariser(int year, int month, int day)
	{
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public DBFile summarise()
	{
		DBFile[] files = new DBFile[60];
		
		int exists = 0;
		
		for (int hour = 0; hour < 24; hour++) {
			String fileName = String.format(
					this.s2Type.toString().toLowerCase() + "/"
					+ this.sType.toString().toLowerCase() +
					"/hour/%04d%02d%02d_%02d.txt", year, month, day, hour);

			DBFile dbFile = null;
			try {
				dbFile = DBFile.readSummary(fileName, this.s2Type);
				dbFile.setDateTime(LocalDateTime.of(year, month, day, hour, 0, 0));
				exists++;
			} catch (IOException e) {
			}
			files[hour] = dbFile;
		}
		
		System.out.println("DEBUG: Found " + exists + " DBFiles.");
		
		if (exists == 0) {
			return null;
		}
		
		ArrayList<DataPoint> dps = summarise(files);
		
		if (dps.size() == 0) {
			return null;
		}
		
		DBFile newFile = new DBFile();
		newFile.setDataPoints(dps);
		
		return newFile;
	}
}