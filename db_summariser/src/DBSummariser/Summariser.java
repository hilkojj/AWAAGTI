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
		int dpIndex = -1;
		while (going) {
			dpIndex++;

			DataPoint dp = new DataPoint();
			dp.summaryType = this.s2Type;
			
			int clientID = -1;
			
			int val = 0;
			LocalDateTime maxDateTime = null;
			
			going = false;
			
			for (int i = 0; i < 60; i++) {
				DataPoint tDP = null;

				if (files[i] != null && files[i].getDataPoints().size() >= dpIndex+1) {
					tDP = files[i].getDataPoints().get(dpIndex);
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

				int newVal = this.check(val, tDP.getVal(this.s2Type));
				if (newVal != val) {
					val = newVal;

					if (tDP.summaryDateTime != null) {
						maxDateTime = tDP.summaryDateTime;
					} else {
						maxDateTime = files[i].getDateTime();
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
	
	private int check(int val1, int val2)
	{
		switch (this.sType) {
		case MAX:
			if (val1 > val2) {
				return val1;
			}
			return val2;
		case MIN:
			if (val1 < val2) {
				return val1;
			}
			return val2;
		default:
			return 0;
		}
	}
}
