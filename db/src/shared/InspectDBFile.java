package shared;

import java.io.File;
import java.io.IOException;

public class InspectDBFile
{
	public static void main(String[] args) throws IOException
	{
		if (args.length == 0) {
			System.out.println("Usage: InspectDBFile {path to .awaagti file} [summaryType (temp) (optional)]");
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
