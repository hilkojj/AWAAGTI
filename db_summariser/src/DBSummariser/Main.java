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
		
		if (args.length < 1) {
			System.out.println("Usage: db_summariser {unix time stamp: from} [unix time stamp: to, defaults to Now if not specified]");
			System.out.println("Generates summary files for the minutes, hours and days between the given timestamps.");
			System.out.println("ERROR: Please provide unixtimestamp of when to start making summuries.");

			return;
		}

		long fromUTS = Long.parseLong(args[0]);
		from = LocalDateTime.ofEpochSecond(fromUTS, 0, ZoneOffset.ofHours(0));
		
		if (args.length >= 2) {
			long toUTS = Long.parseLong(args[1]);
			to = LocalDateTime.ofEpochSecond(toUTS, 0, ZoneOffset.ofHours(0));
		} else {
			to = LocalDateTime.now();
		}
		
		boolean dryRun = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
		
		System.out.println("Start db_summariser");
		System.out.print("Summarise from ");
		System.out.print(from);
		System.out.print(" to ");
		System.out.println(to);
		
		if (dryRun) {
			System.out.println("Dry run!");
		}
		
		LocalDateTime now = from;
		
		int year = 0;
		int month = 0;
		int day = 0;
		boolean firstDay = false;
		boolean lastDay = false;
		while (!now.isAfter(to)) {
			year = now.getYear();
			month = now.getMonthValue();
			day = now.getDayOfMonth();
			
			firstDay = from.getYear() == year && from.getMonthValue() == month && from.getDayOfMonth() == day;
			lastDay = to.getYear() == year && to.getMonthValue() == month && to.getDayOfMonth() == day;
			
			System.out.println();
			System.out.println(" ---- " + year + " " + month + " " + day + " , is first: " + firstDay + " , is last: " + lastDay);
			System.out.println();
			
			for (int hour = 0; hour < 24; hour++) {
				if (firstDay && from.getHour() > hour) {
					System.out.println(" - Skipping Hour: " + hour);
					continue;
				}
								
				boolean firstHour = firstDay && from.getHour() == hour;
				boolean lastHour = lastDay && to.getHour() == hour;
				
				System.out.println();
				System.out.println(" --- " + hour + " , is first: " + firstHour + " , is last: " + lastHour);

				for (int minute = 0; minute < 60; minute++) {
					if (firstHour && from.getMinute() > minute) {
						System.out.println(" - Skipping minute: " + minute);
						continue;
					}
					if (lastHour && minute >= to.getMinute()) {
						System.out.println(" - Skipping all minutes: " + minute);
						break;
					}

					System.out.println(" -- Summarise Minute: " + minute);
					if (!dryRun) {
						summariseMinute(year, month, day, hour, minute);
					}
				}
				
				if (lastDay && lastHour) {
					break;
				}
				
				System.out.println(" -- Summarise Hour: " + hour);
				if (!dryRun) {
					summariseHour(year, month, day, hour);
				}
			}
			
			if (lastDay && day >= to.getDayOfMonth()) {
				break;
			}
			
			System.out.println(" -- Summarise Day: " + day);
			
			if (!dryRun) {
				summariseDay(year, month, day);
			}
			
			now = now.plusDays(1);
		}
		
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
				
				sum.setSummaryTypes(sType, s2Type);
				DBFile dbFile = sum.summarise();
				if (dbFile == null) {
					System.out.println("No summary, nothing to save.");
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