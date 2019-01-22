package DBSummariser;

import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class Summariser
{
	enum SummaryType
	{
		MIN, MAX
	}
	
	protected SummaryType sType;
	protected DataPoint.SummaryType s2Type;

	abstract public DBFile summarise();
	
	public void setSummaryTypes(SummaryType sType, DataPoint.SummaryType s2Type)
	{
		this.sType = sType;
		this.s2Type = s2Type;
	}
	
	protected ArrayList<DataPoint> summarise(DBFile[] files)
	{
		ArrayList<DataPoint> dps = new ArrayList<DataPoint>(); // TODO: pre allocate.
		
		int[] fileDPSIndexes = new int[files.length];
		
		boolean going = true;
		//int dpIndex = -1; // DataPoint index.
		while (going) { // Loop over DataPoints in files.
			//dpIndex++;

			DataPoint dp = new DataPoint();
			dp.summaryType = this.s2Type;
			
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

				Integer newVal = this.check(val, tDP.getVal(this.s2Type));
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
	
	private Integer check(Integer val1, int val2)
	{
		if (val1 == null) {
			return val2;
		}

		switch (this.sType) {
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
			System.out.println("ERROR: invalid sType in method check: " + this.sType);
			return 0;
		}
	}
}
