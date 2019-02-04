package shared;

import java.io.File;
import java.io.IOException;

class DBFileTest
{
	public static void main(String[] args) throws IOException
	{
		File file = new File("15/48/69/06/1548690685."+Settings.DATA_EXTENSION);
		DBFile dbFile = DBFile.read(file);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.getClientID() + ": " + db.getTemp());
		}
		
		file = new File("15/48/69/06/temp_max_sum."+Settings.DATA_EXTENSION);
		dbFile = DBFile.read(file, DBValue.TEMP);
		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.getClientID() + ": " + db.getTemp() + " " + db.getSummaryDateTime());
		}
	}
}
