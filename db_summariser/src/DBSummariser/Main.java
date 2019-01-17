package DBSummariser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.stream.Stream;

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
		}

		long fromUTS = Long.parseLong(args[1]);
		from = LocalDateTime.ofEpochSecond(fromUTS, 0, null);

		if (args.length >= 3) {
			long toUTS = Long.parseLong(args[2]);
			to = LocalDateTime.ofEpochSecond(toUTS, 0, null);
		} else {
			to = LocalDateTime.now();
		}
		
		LocalDateTime now = from;
		
		while (!now.isAfter(to)) {
			for (int hour = 0; hour < 24; hour++) {
				for (int minute = 0; minute < 60; minute++) {
					
				}
				
			}
			
			now.plusDays(1);
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
	
	private boolean summariseMinute(int year, int month, int day, int hour, int minute)
	{
		File f = new File(String.format("min/minute/%d%d%d_%d%d.txt", year, month, day, hour, minute));
		if (f.exists() && !f.isDirectory()) { 
			return false;
		}
		
		for (int second = 0; second < 60; second++) {
			Path fileName = Path.of(
					String.format("min/minute/%d%d%d_%d%d%d.txt", year, month, day, hour, minute, second));
			
			String line;

			try (Stream<String> lines = Files.lines(fileName)) {
	            line = lines.skip(1).findFirst().get();
	            System.out.println(line);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		
		return true;
	}
}
