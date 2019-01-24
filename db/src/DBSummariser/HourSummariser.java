package DBSummariser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import shared.DBFile;
import shared.DataPoint;

public class HourSummariser extends Summariser
{
	private int year, month, day, hour;
	
	public HourSummariser(int year, int month, int day, int hour)
	{
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
	}

	public DBFile summarise()
	{
		// TODO: Make summariser an interface!
		DBFile[] files = new DBFile[60];
		
		int exists = 0;
		
		for (int minute = 0; minute < 60; minute++) {
			String fileName = String.format(
					this.s2Type.toString().toLowerCase() + "/"
					+ this.sType.toString().toLowerCase() +
					"/minute/%04d%02d%02d_%02d%02d.txt", year, month, day, hour, minute);

			DBFile dbFile = DBFile.readSummary(fileName, this.s2Type);
			if (dbFile != null) {
				dbFile.setDateTime(LocalDateTime.of(year, month, day, hour, minute, 0));
				exists++;
			}
			files[minute] = dbFile;
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