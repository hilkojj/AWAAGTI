package DBSummariser;

import java.time.LocalDateTime;
import java.util.ArrayList;

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
		
		boolean atLeastOneExists = false;
		
		for (int day = 0; day < 60; day++) {
			String fileName = String.format("min/hour/%04d%02d%02d_%02d.txt", year, month, day, hour, hour);
			DBFile dbFile = DBFile.readSummary(fileName, DataPoint.SummaryType.TEMP);
			if (dbFile != null) {
				dbFile.setDateTime(LocalDateTime.of(year, month, day, hour, 0, 0));
				atLeastOneExists = true;
			}
			files[hour] = dbFile;

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