package shared;

import java.io.File;
import java.io.IOException;

/**
 * InspectDBFile allows for printing the contents of a binary DBFile
 * in human readable format. It's used for debugging.
 * 
 * @author remi
 */
public class InspectDBFile
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0) {
			System.out.println("Usage: InspectDBFile {path to .awaagti file} [summaryType (temp, wind, etc) (optional, but required if database file is a summary)]");
			return;
		}
		
		DBValue summaryType = null;

		File f = new File(args[0]);
		if (args.length >= 2) {
			summaryType = DBValue.valueOf(args[1].toUpperCase());
		}

		DBFile dbFile = DBFile.read(f, summaryType);

		for (DataPoint dp: dbFile.getDataPoints()) {
			System.out.println(dp.toString());
		}
	}
}
