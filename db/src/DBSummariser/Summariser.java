package DBSummariser;


import shared.DBFile;
import shared.DBValue;
import shared.DataPoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Summariser
{
	enum SummaryType
	{
		MIN, MAX
	}
	
	protected long unixTime;
	protected String dir;
	
	protected SummaryType sType;
	protected DBValue s2Type;
	
	protected DBFile[] files;
	
	public Summariser(long unixTime)
	{
		this.unixTime = unixTime;
		this.dir = DBFile.dirForUTS(unixTime);
	}
	
	public SummaryType getsType() {
		return sType;
	}

	public void setsType(SummaryType sType) {
		this.sType = sType;
	}

	public DBValue getS2Type() {
		return s2Type;
	}

	public void setS2Type(DBValue s2Type) {
		this.s2Type = s2Type;
	}
	
	public boolean alreadyExists()
	{
		File f = new File(this.makeFileName());
		return f.exists() && !f.isDirectory();
	}
	
	private String makeFileName()
	{
		String fileName = this.s2Type.toString().toLowerCase() + "_" + this.sType.toString().toLowerCase() + "_sum";
		String sumFileName = this.dir + "/" + fileName + ".awaagti";

		return sumFileName;
	}

	public void summarise()
	{
		ArrayList<DataPoint> dps = this.summariseActually();
		if (dps.size() == 0) {
			System.out.println("No summary, nothing to save.");
			return;
		}

		DBFile dbFile = new DBFile();
		dbFile.setDataPoints(dps);

		File directory = new File(dir);
		if (!directory.exists()){
			System.out.println("Make dir");
			directory.mkdirs();
		}

		dbFile.setFileName(this.makeFileName());
		try {
			dbFile.write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int readFiles()
	{
		DBFile[] files = new DBFile[100];
		
		int exists = 0;
		String fileName = "";
		
		DBFile dbFile;
		for (int second = 0; second < 100; second++) {
			fileName = this.makeReadFilesFileName(second);
			
			dbFile = null;
			try {
				File f = new File(fileName);
				dbFile = DBFile.read(f);
				// TODO: check why this is needed.
				dbFile.setDateTime(this.unixTime+second);
				exists++;
			} catch (IOException e) {
			}
			files[second] = dbFile;
		}
		
		System.out.println("DEBUG: Found " + exists + " DBFiles. " + fileName);
		
		this.files = files;
		
		return exists;
	}
	
	protected ArrayList<DataPoint> summariseActually()
	{
		ArrayList<DataPoint> dps = new ArrayList<DataPoint>(); // TODO: pre allocate.
		
		int[] fileDPSIndexes = new int[this.files.length];
		
		boolean going = true;
		//int dpIndex = -1; // DataPoint index.
		while (going) { // Loop over DataPoints in files.
			//dpIndex++;

			DataPoint dp = new DataPoint();
			dp.setSummaryType(s2Type);
			
			// Determine next lowest clientID.
			// (Because client can be missing from DBFiles)
			int clientID = -1;
			int i = -1;
			for (DBFile file : this.files) {
				i++;
				if (file == null) {
					continue;
				}
				
				if (file.getDataPoints().size() < fileDPSIndexes[i]+1) {
					continue;
				}
				
				int ourLowestClientID  = file.getDataPoints().get(fileDPSIndexes[i]).getClientID();
				if (clientID != -1 && clientID <= ourLowestClientID) {
					continue;
				}
				
				clientID = ourLowestClientID;
			}
			
			Integer val = null;
			long maxDateTime = 0;
			
			going = false;
			
			i = -1;
			for (DBFile file : this.files) {
				i++;
				if (file == null) {
					continue;
				}
				
				if (file.getDataPoints().size() < fileDPSIndexes[i]+1) {
					continue;
				}

				DataPoint tDP = file.getDataPoints().get(fileDPSIndexes[i]);
				
				if (clientID != tDP.getClientID()) {
					// ClientID is missing from this DBFile :(, but that's okay.
					// All DPS's are sorted, so if the clientID is not .
					
					System.out.println("DEBUG: missing clientID " + clientID + ", got " + tDP.getClientID() + " at " + i);
					continue;
				}
				
				going = true;
				
				fileDPSIndexes[i]++;

				Integer newVal = check(this.sType, val, tDP.getVal(this.s2Type));
				if (newVal != null) {
					val = newVal;
					
					if (tDP.getSummaryDateTime() != 0) {
						maxDateTime = tDP.getSummaryDateTime();
					} else {
						maxDateTime = file.getDateTime();
					}
				}	
	
			}
			
			if (!going) {
				continue;
			}

			dp.setClientID(clientID);
			
			if (maxDateTime != 0) {
				dp.setTemp(val);
				dp.setSummaryDateTime(maxDateTime);
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
	
	protected String makeReadFilesFileName(int second)
	{
		return String.format(dir + "%d.awaagti", this.unixTime+second);
	}
}