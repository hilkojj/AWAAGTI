package shared;

import java.io.IOException;

public class DBFileTest
{
	public static void main(String[] args) throws IOException
	{
		DBFile dbFile = DBFile.read("1538399520.txt");
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.clientID + ": " + db.temp);
		}
		
		dbFile = DBFile.readSummary("temp/min/hour/20181001_13.txt", DataPoint.SummaryType.TEMP);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.clientID + ": " + db.temp + " " + db.summaryDateTime);
		}
	}
}
