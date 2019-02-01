package shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class DBFile
{
	private ArrayList<DataPoint> dataPoints = new ArrayList<DataPoint>();
	
	private String fileName;
	
	private long dateTime;
	
	public static DBFile read(File file) throws IOException
	{
		DBFile dbFile = new DBFile();
		dbFile.readFile(file, null, null, null);
		return dbFile;
	}
	
	public static DBFile read(File file, DBValue summaryType) throws IOException
	{
		DBFile dbFile = new DBFile();
		dbFile.readFile(file, summaryType, null, null);
		return dbFile;
	}
	
	public static DBFile read(File file, DBValue summaryType, int[] filterClientIDs, QueryFilter filter) throws IOException
	{
		DBFile dbFile = new DBFile();
		dbFile.readFile(file, summaryType, filterClientIDs, filter);
		return dbFile;
	}
	
	private void readFile(File file, DBValue summaryType, int[] filterClientIDs, QueryFilter filter) throws IOException
	{
		this.dataPoints = new ArrayList<DataPoint>();

        InputStream inputStream = new FileInputStream(file);

		byte[] lengths = new byte[1];
		inputStream.read(lengths, 0, 1);
		byte length = lengths[0];

        byte[] byteRead = new byte[length];
        int read;

    	while (true) {
        	read = inputStream.read(byteRead, 0, length); // OLD
        	if (read == -1 || read == 0) {
				break;
			}

//			Logger.error(read);
        	
        	DataPoint dp = DataPoint.fromDBLine(byteRead, summaryType);


			IntStream clientIDs = null;
			if (filterClientIDs != null) {
				clientIDs = IntStream.of(filterClientIDs);
			}

			if (clientIDs != null) {
				boolean match = clientIDs.anyMatch(x -> x == dp.getClientID());
//				Logger.error("MATCH: " + match);
				if (!match) {
					continue;
				}
			}

        	if (filter != null && !filter.execute(dp)) {
        		continue;
        	}

        	this.dataPoints.add(dp);
		}
    	
    	inputStream.close();
	}
	
	/**
	 * Writer formats the DataPoint data and writes the file to the filesystem.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		FileOutputStream writer = new FileOutputStream(this.fileName);

		byte lineLength = 0;
		if (this.dataPoints != null && this.dataPoints.size() > 0) {
			lineLength = (byte)this.dataPoints.get(0).makeDBLine().length;
		}

		writer.write(new byte[] {lineLength});

		for (DataPoint dp : this.dataPoints) {
			byte[] data = dp.makeDBLine();
			if (data.length != lineLength) {
				Logger.error("HAHAHAAHHAHA DIT IS KUT " + data.length + " : " + lineLength);
			}
			
			//writer.write(padRight(dp.makeDBLine(), highest) + "\n");
			writer.write(data);
		}
		 
		writer.close();
	}
	
	public void setFileName(String newFileName)
	{
		this.fileName = newFileName;
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public long getDateTime()
	{
		return dateTime;
	}

	public void setDateTime(long dateTime)
	{
		this.dateTime = dateTime;
	}	
	
	public ArrayList<DataPoint> getDataPoints()
	{
		return this.dataPoints;
	}
	
	public void setDataPoints(ArrayList<DataPoint> dps)
	{
		this.dataPoints = dps;
	}
	
	public static String dirForUTS(long uts)
	{
		String[] items = (uts + "").split("(?<=\\G..)");
		return String.join("/", Arrays.copyOf(items, items.length-1)) + "/";
	}
}