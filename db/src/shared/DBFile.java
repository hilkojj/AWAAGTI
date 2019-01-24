package shared;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

public class DBFile
{
	private ArrayList<DataPoint> dataPoints;
	
	private String fileName;
	
	private LocalDateTime dateTime;
	
	public static DBFile readSummary(String fileName, DataPoint.SummaryType summaryType)
	{
		return read(fileName, summaryType);
	}
	
	public static DBFile read(String fileName)
	{
		return read(fileName, null);
	}
	
	private static DBFile read(String fileName, DataPoint.SummaryType summaryType)
	{
		DBFile dbFile = new DBFile();

		dbFile.dataPoints = new ArrayList<DataPoint>();
		
		Path path = Paths.get(fileName);

		try (Stream<String>  lines = Files.lines(path)) {
	        lines.forEachOrdered(line->dbFile.interpretLine(line, summaryType));
		} catch (IOException e) {
			return null;
		}
		
		return dbFile;	
	}
	
	private void interpretLine(String line, DataPoint.SummaryType summaryType)
	{
		String[] val = line.split("=");
		if (val.length < 2) {
			return;
		}
		
		line = line.replace("#", "");
		
		DataPoint dp = DataPoint.fromDBLine(line, summaryType);
		this.dataPoints.add(dp);
	}
	
	
	
	/**
	 * Writer formats the DataPoint data and writes the file to the filesystem.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(this.fileName));
		
		// Determine required line length
		int highest = 0;
		int length;
		for (DataPoint dp : this.dataPoints) {
			length = dp.makeDBLine().length();
			if (length > highest) {
				highest = length;
			}
		}
		
		writer.write(padRight(highest+1 + "", highest) + "\n");
		// (highest+1, because the linebreak is not included in 'highest'
		//   but is part of the line length of course)
		
		for (DataPoint dp : this.dataPoints) {
			writer.write(padRight(dp.makeDBLine(), highest) + "\n");
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
}