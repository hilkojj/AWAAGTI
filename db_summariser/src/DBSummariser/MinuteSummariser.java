package DBSummariser;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class MinuteSummariser extends Summariser
{
	private int year, month, day, hour, minute;
	
	public MinuteSummariser(int year, int month, int day, int hour, int minute)
	{
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.minute = minute;
	}

	public DBFile summarise()
	{
		DBFile[] files = new DBFile[60];
		
		boolean atLeastOneExists = false;
		
		for (int second = 0; second < 60; second++) {
			String fileName = String.format("%04d%02d%02d_%02d%02d%02d.txt", year, month, day, hour, minute, second);
			DBFile dbFile = DBFile.read(fileName);
			if (dbFile != null) {
				dbFile.setDateTime(LocalDateTime.of(year, month, day, hour, minute, second));
				atLeastOneExists = true;
			}
			files[second] = dbFile;

			if (dbFile == null) { 
				System.out.println("Main: DBFile does not exist: " + fileName);
			}
		}
		
		if (!atLeastOneExists) {
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
