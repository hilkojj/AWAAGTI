package DBSummariser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import shared.DBFile;
import shared.DataPoint;

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
		
		long uts = LocalDateTime.of(year, month, day, hour, minute, 0).toEpochSecond(ZoneOffset.UTC);
		
		int exists = 0;
		
		long thisUTS;
		String dir;
		String fileName;
		DBFile dbFile;
		for (int second = 0; second < 60; second++) {
			thisUTS = uts + second;
			
			dir = DBFile.dirForUTS(thisUTS);

			fileName = String.format(dir + "%d.txt", uts+second);

			dbFile = null;
			try {
				dbFile = DBFile.read(fileName);
				dbFile.setDateTime(LocalDateTime.of(year, month, day, hour, minute, second));
				exists++;
			} catch (IOException e) {
			}
			files[second] = dbFile;
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
