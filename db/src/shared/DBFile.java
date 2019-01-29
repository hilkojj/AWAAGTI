package shared;


import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

public class DBFile
{
	private ArrayList<DataPoint> dataPoints;
	
	private String fileName;
	
	private LocalDateTime dateTime;
	
	public static DBFile readSummary(String fileName, DataPoint.SummaryType summaryType) throws IOException
	{
		return read(fileName, summaryType);
	}
	
	public static DBFile read(String fileName) throws IOException
	{
		return read(fileName, null);
	}
	
	private static DBFile read(String fileName, DataPoint.SummaryType summaryType) throws IOException
	{
		DBFile dbFile = new DBFile();

		dbFile.dataPoints = new ArrayList<DataPoint>();

		File f = new File(fileName);

        InputStream inputStream = new FileInputStream(f);

        byte[] lengths = new byte[]{Byte.valueOf("a")}; // NEW
        inputStream.read(lengths, 0, 1);
		byte length = lengths[0];

//		byte length = inputStream.readNBytes(1)[0];  // OLD

        System.out.println("Part length: " + length);

        byte[] byteRead = new byte[length];
        int i = 0;

    	while (true) {
			inputStream.read(byteRead);	// NEW
//        	byteRead = inputStream.readNBytes(length); // OLD
        	if (byteRead.length < length) {
        		System.out.println("What? Less than expected length: " + byteRead.length);
        		break;
        	}

        	dbFile.interpretLine(byteRead, summaryType);
		}

    	inputStream.close();

		return dbFile;
	}
	
	private void interpretLine(byte[] line, DataPoint.SummaryType summaryType)
	{
		DataPoint dp = DataPoint.fromDBLine(line, summaryType);
		this.dataPoints.add(dp);
	}
	
	/**
	 * Writer formats the DataPoint data and writes the file to the filesystem.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		FileOutputStream writer = new FileOutputStream(this.fileName);
		
		writer.write(new byte[] {(byte)this.dataPoints.get(0).makeDBLine().length});
		
		for (DataPoint dp : this.dataPoints) {
			byte[] data = dp.makeDBLine();
			
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
	
	private static String padRight(String s, int n)
	{
	     return String.format("%1$-" + n + "s", s).replace(' ', '#'); 
	}

	public LocalDateTime getDateTime()
	{
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime)
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