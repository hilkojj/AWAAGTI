package DBSummariser;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Main
{
	public static void main(String[] args)
	{
		LocalDateTime from;
		LocalDateTime to;
		
		if (args.length < 2) {
			System.out.println("Usage: db_summariser {unix time stamp: from} [unix time stamp: to, defaults to Now if not specified]");
			System.out.println("Generates summary files for the minutes, hours and days between the given timestamps.");
			System.out.println("ERROR: Please provide unixtimestamp of when to start making summuries.");

			return;
		}

		long fromUTS = Long.parseLong(args[1]);
		from = LocalDateTime.ofEpochSecond(fromUTS, 0, ZoneOffset.UTC);

		if (args.length >= 3) {
			System.out.println("not now");
			long toUTS = Long.parseLong(args[2]);
			to = LocalDateTime.ofEpochSecond(toUTS, 0, ZoneOffset.UTC);
		} else {
			System.out.println("now");
			to = LocalDateTime.now();
		}
		
		System.out.println(from);
		System.out.println(to);
		
		LocalDateTime now = from;
		
		int year = 0;
		int month = 0;
		int day = 0;
		while (!now.isAfter(to)) {
			year = now.getYear();
			month = now.getMonthValue();
			day = now.getDayOfMonth();
			
			for (int hour = 9; hour < 12; hour++) {
				for (int minute = 0; minute < 8; minute++) {
					System.out.println(minute);
					summariseMinute(year, month, day, hour, minute);
				}
				
				summariseHour(year, month, day, hour);
			}
			
			summariseDay(year, month, day);
			
			now = now.plusDays(1);
		}

		// start = new Date(start of universe)
		// current = new Date(current)
		// for each day from start to current
		//    for each hour in day
		//        for each minute in hour
		//           check if summary exists, if not, create it
		//        check if hour summary exists, if not, create it
		//    check if day summary exists, if not, create it
	}
	
	private static boolean summariseMinute(int year, int month, int day, int hour, int minute)
	{
		String sumFileName = String.format("/minute/%02d%02d%02d_%02d%02d.txt", year, month, day, hour, minute);
		Summariser sum = new MinuteSummariser(year, month, day, hour, minute);
		
		return summarise(sumFileName, sum);
	}
	
	private static boolean summariseHour(int year, int month, int day, int hour)
	{
		String sumFileName = String.format("/hour/%02d%02d%02d_%02d.txt", year, month, day, hour);
		Summariser sum = new HourSummariser(year, month, day, hour);
		
		return summarise(sumFileName, sum);
	}
	
	private static boolean summariseDay(int year, int month, int day)
	{
		String sumFileName = String.format("/day/%02d%02d%02d.txt", year, month, day);
		Summariser sum = new DaySummariser(year, month, day);
		
		return summarise(sumFileName, sum);
	}
	
	private static boolean summarise(String fileName, Summariser sum)
	{
		for (Summariser.SummaryType sType : Summariser.SummaryType.values()) {
			for (DataPoint.SummaryType s2Type : DataPoint.SummaryType.values()) {
				String sumFileName = s2Type.toString().toLowerCase() + "/" + sType.toString().toLowerCase() + fileName;

				File f = new File(sumFileName);
				if (f.exists() && !f.isDirectory()) { 
					System.out.println(sumFileName + ": already exists");
					continue;
				}
				
				DBFile dbFile = sum.summarise(sType, s2Type);
				if (dbFile == null) {
					continue;
				}

				dbFile.setFileName(sumFileName);
				try {
					dbFile.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return true;
	}
}
