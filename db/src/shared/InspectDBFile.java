package shared;

import java.io.File;
import java.io.IOException;

public class InspectDBFile
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0) {
			System.out.println("Provide unix time pl0x");
			System.out.println("Usage: InspectDBFile {unix time} [summaryType (temp) (optional)] [summaryType (max) (optional)]");
		}
		
		long uts = Long.parseLong(args[0]);
		String dir = DBFile.dirForUTS(uts);
		
		File f;
		DBFile dbFile;
		if (args.length == 3) {
			f = new File(String.format("%s/%s_%s_sum.awaagti", dir, args[1], args[2]));
			dbFile = DBFile.read(f, DataPoint.SummaryType.TEMP);
		} else {
			f = new File(String.format("%s/%d.awaagti", dir, uts));
			dbFile = DBFile.read(f);
		}

		for (DataPoint db: dbFile.getDataPoints()) {
			System.out.println(db.clientID + ": " + db.temp);
		}
	}
}
