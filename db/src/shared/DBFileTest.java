package shared;

import java.io.File;
import java.io.IOException;

public class DBFileTest
{
	public static void main(String[] args) throws IOException
	{
		File file = new File("15/38/39/94/1538399400.txt");
		DBFile dbFile = DBFile.read(file);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.clientID + ": " + db.temp);
		}
		
		file = new File("temp/min/hour/20181001_13.txt");
		dbFile = DBFile.read(file, DataPoint.SummaryType.TEMP);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.clientID + ": " + db.temp + " " + db.summaryDateTime);
		}
	}
}
