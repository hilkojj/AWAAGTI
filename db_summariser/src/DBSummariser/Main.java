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
			
			for (int hour = 9; hour < 24; hour++) {
				for (int minute = 0; minute < 60; minute++) {
					System.out.println(minute);
					summariseMinute(year, month, day, hour, minute);
				}
				
				summariseHour(year, month, day, hour);
			}
			
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
		String sumFileName = String.format("min/minute/%02d%02d%02d_%02d%02d.txt", year, month, day, hour, minute);

		File f = new File(sumFileName);
		if (f.exists() && !f.isDirectory()) { 
			System.out.println(sumFileName + ": already exists");
			return false;
		}
		
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
			return false;
		}
		
		ArrayList<DataPoint> dps = summarise(files);
		
		if (dps.size() == 0) {
			return false;
		}
		
		DBFile newFile = new DBFile();
		newFile.setFileName(sumFileName);
		newFile.setDataPoints(dps);
		try {
			newFile.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
	private static boolean summariseHour(int year, int month, int day, int hour)
	{
		// TODO: Make summariser an interface!
		return false;
	}
	
	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:MM:ss");

	private static ArrayList<DataPoint> summarise(DBFile[] files)
	{
		ArrayList<DataPoint> dps = new ArrayList<DataPoint>(); // TODO: pre allocate.
		int[] indexes = new int[60];

		while (true) {
			int lowestClientID = -1;

			boolean hasLeft = false;

			for (int i = 0; i < 60; i++) {
				if (files[i] == null) {
					continue;
				}
				
				if (files[i].getDataPoints().size() <= indexes[i]+1) {
					continue;
				}

				DataPoint dp = files[i].getDataPoints().get(indexes[i]);
				if (dp == null) {
					continue;
				}

				hasLeft = true;

				int cid = dp.clientID;
				if (lowestClientID == -1 || cid < lowestClientID) {
					lowestClientID = cid;
				}
			}
			
			if (!hasLeft) {
				break;
			}
			
			DataPoint dp = new DataPoint();
			dp.clientID = lowestClientID;
			
			int max = 0;
			LocalDateTime maxDateTime = null;
			
			for (int i = 0; i < 60; i++) {
				int cid = -1;
				DataPoint tDP = null;
				do {
					if (files[i] != null) {
						tDP = files[i].getDataPoints().get(indexes[i]);
					}
					indexes[i]++;
					if (tDP == null) {
						continue;
					}

					cid = tDP.clientID;
				} while (cid != lowestClientID);

				if (tDP != null) {
					if (tDP.temp > max) {
						max += 1;
						maxDateTime = files[i].getDateTime();
					}
				}
			}
			
			if (maxDateTime != null) {
				dp.summary = new String[]{max + "", maxDateTime.format(dateFormatter), maxDateTime.format(timeFormatter)};
			}
			
			dps.add(dp);
		}
		
		return dps;
	}
}
