package DBSummariser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

import shared.DBFile;
import shared.DataPoint;

public class Main
{
	public static void main(String[] args)
	{
		System.out.println("@@@ YOU WERE WORKING ON MAKING THIS WORK. IT GENERATES THE FILES, BUT FOR SOME REASON DBFileTest says they're with payload (=temp) 0");
		
		LocalDateTime from;
		LocalDateTime to;
		
		if (args.length < 1) {
			System.out.println("Usage: db_summariser {unix time stamp: from} [unix time stamp: to, defaults to Now if not specified]");
			System.out.println("Generates summary files for the minutes, hours and days between the given timestamps.");
			System.out.println("ERROR: Please provide unixtimestamp of when to start making summuries.");

			return;
		}

		long fromUTS = Long.parseLong(args[0]);
		long toUTS;
		
		if (args.length >= 2) {
			toUTS = Long.parseLong(args[1]);
		} else {
			toUTS = (int)(System.currentTimeMillis()/1000);
		}
		
		boolean dryRun = args.length >= 3 && args[2].equalsIgnoreCase("dryrun");
		
		System.out.println("Start db_summariser");
		System.out.print("Summarise from ");
		System.out.print(fromUTS);
		System.out.print(" to ");
		System.out.println(toUTS);
		
		if (dryRun) {
			System.out.println("Dry run!");
		}
		
		long now = fromUTS/100*100;
		for (; now <= toUTS; now+=100) {
			//summarise(DBFile.dirForUTS(now), now, false);
			
			if (now % 10000 == 0) {
				System.out.println("Made it");
				summarise(DBFile.dirForUTS(now/100), now, true);
				return;
			}
			if (now % 1000000 == 0) {
				summarise(DBFile.dirForUTS(now/10000), now, true);
				return;
			}
			if (now % 100000000 == 0) {
				summarise(DBFile.dirForUTS(now/1000000), now, true);
				return;
			}
		}
	}
	
	private static void summarise(String dir, long uts, boolean ofSummaries)
	{
		for (Summariser.SummaryType sType : Summariser.SummaryType.values()) {
			for (DataPoint.SummaryType s2Type : DataPoint.SummaryType.values()) {
				String fileName = s2Type.toString().toLowerCase() + "_" + sType.toString().toLowerCase() + "_sum";
				String sumFileName = dir + "/" + fileName + ".awaagti";

				File f = new File(sumFileName);
				if (f.exists() && !f.isDirectory()) { 
					System.out.println(sumFileName + ": already exists");
					continue;
				}
				
				DBFile[] inputs = readFiles(dir, uts, ofSummaries, fileName);
				if (inputs == null) {
					System.out.println("No files read :( ");
					continue;
				}
				
				ArrayList<DataPoint> dps = summariseActually(inputs, sType, s2Type);
				if (dps.size() == 0) {
					System.out.println("No summary, nothing to save.");
					continue;
				}
				
				DBFile dbFile = new DBFile();
				dbFile.setDataPoints(dps);
				
				File directory = new File(dir);
			    if (!directory.exists()){
			    	System.out.println("Make dir");
			        directory.mkdirs();
			    }

				dbFile.setFileName(sumFileName);
				try {
					dbFile.write();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static DBFile[] readFiles(String dir, long uts, boolean ofSummarized, String fileNameBase)
	{
		DBFile[] files = new DBFile[100];
		
		int exists = 0;
		String fileName;
		
		DBFile dbFile;
		for (int second = 0; second < 100; second++) {
			if (ofSummarized) {
				fileName = String.format("%s/%02d/%s.awaagti", dir, second, fileNameBase);
			} else {
				fileName = String.format(dir + "%d.awaagti", uts+second);
			}
			
			dbFile = null;
			try {
				File f = new File(fileName);
				dbFile = DBFile.read(f);
				// TODO: check why this is needed.
				//dbFile.setDateTime(LocalDateTime.of(year, month, day, hour, minute, second));
				exists++;
			} catch (IOException e) {
			}
			files[second] = dbFile;
		}
		
		System.out.println("DEBUG: Found " + exists + " DBFiles.");
		
		if (exists == 0) {
			return null;
		}
		
		return files;
	}
	
	protected static ArrayList<DataPoint> summariseActually(DBFile[] files, Summariser.SummaryType sType, DataPoint.SummaryType s2Type)
	{
		ArrayList<DataPoint> dps = new ArrayList<DataPoint>(); // TODO: pre allocate.
		
		int[] fileDPSIndexes = new int[files.length];
		
		boolean going = true;
		//int dpIndex = -1; // DataPoint index.
		while (going) { // Loop over DataPoints in files.
			//dpIndex++;

			DataPoint dp = new DataPoint();
			dp.summaryType = s2Type;
			
			// Determine next lowest clientID.
			// (Because client can be missing from DBFiles)
			int clientID = -1;
			int i = -1;
			for (DBFile file : files) {
				i++;
				if (file == null) {
					continue;
				}
				
				if (file.getDataPoints().size() < fileDPSIndexes[i]+1) {
					continue;
				}
				
				int ourLowestClientID  = file.getDataPoints().get(fileDPSIndexes[i]).clientID;
				if (clientID != -1 && clientID <= ourLowestClientID) {
					continue;
				}
				
				clientID = ourLowestClientID;
			}
			
			Integer val = null;
			LocalDateTime maxDateTime = null;
			
			going = false;
			
			i = -1;
			for (DBFile file : files) {
				i++;
				if (file == null) {
					continue;
				}
				
				if (file.getDataPoints().size() < fileDPSIndexes[i]+1) {
					continue;
				}

				DataPoint tDP = file.getDataPoints().get(fileDPSIndexes[i]);
				
				if (clientID != tDP.clientID) {
					// ClientID is missing from this DBFile :(, but that's okay.
					// All DPS's are sorted, so if the clientID is not .
					
					System.out.println("DEBUG: missing clientID " + clientID + ", got " + tDP.clientID + " at " + i);
					continue;
				}
				
				going = true;
				
				fileDPSIndexes[i]++;

				Integer newVal = check(sType, val, tDP.getVal(s2Type));
				if (newVal != null) {
					val = newVal;
					
					if (tDP.summaryDateTime != null) {
						maxDateTime = tDP.summaryDateTime;
					} else {
						maxDateTime = file.getDateTime();
					}
				}	
	
			}
			
			if (!going) {
				continue;
			}

			dp.clientID = clientID;
			
			if (maxDateTime != null) {
				dp.temp = val;
				dp.summaryDateTime = maxDateTime;
			}
			
			dps.add(dp);
		}
		
		return dps;
	}
	
	private static Integer check(Summariser.SummaryType sType, Integer val1, int val2)
	{
		if (val1 == null) {
			return val2;
		}

		switch (sType) {
		case MAX:
			if (val2 > val1) {
				return val2;
			}
			return null;
		case MIN:
			if (val2 < val1) {
				return val2;
			}
			return null;
		default:
			System.out.println("ERROR: invalid sType in method check: " + sType);
			return 0;
		}
	}
}