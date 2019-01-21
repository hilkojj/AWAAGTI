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
		
		boolean going = true;
		int dpIndex = -1; // DataPoint index.
		while (going) { // Loop over DataPoints in files.
			dpIndex++;

			DataPoint dp = new DataPoint();
			dp.summaryType = this.s2Type;
			
			int clientID = -1;
			
			Integer val = null;
			LocalDateTime maxDateTime = null;
			
			going = false;
			
			for (DBFile file : files) {
				DataPoint tDP = null;

				if (file != null && file.getDataPoints().size() >= dpIndex+1) {
					tDP = file.getDataPoints().get(dpIndex);
				}

				if (tDP == null) {
					continue;
				}
				
				if (clientID == -1) {
					clientID = tDP.clientID;
				}
				
				if (clientID != tDP.clientID) {
					System.out.println("ERROR: unexpected clientID. Wants: " + clientID + ", got " + tDP.clientID);
				}
				
				going = true;

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
