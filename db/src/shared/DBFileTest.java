package shared;

import java.io.File;
import java.io.IOException;

public class DBFileTest
{
	public static void main(String[] args) throws IOException
	{
		File file = new File("15/48/69/06/1548690685.awaagti");
		DBFile dbFile = DBFile.read(file);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.getClientID() + ": " + db.getTemp());
		}
		
		file = new File("15/48/69/06/temp_max_sum.awaagti");
		dbFile = DBFile.read(file, DataPoint.SummaryType.TEMP);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.getClientID() + ": " + db.getTemp() + " " + db.getSummaryDateTime());
		}
	}
}
