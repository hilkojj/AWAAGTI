package DBSummariser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Stream;

public class DBFile
{
	private ArrayList<DataPoint> dataPoints;
	private LocalDateTime dateTime;
	
	private DateTimeFormatter dateTimeFormatter =
			DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	
	public static DBFile read(String fileName)
	{
		DBFile dbFile = new DBFile();

		dbFile.dataPoints = new ArrayList<DataPoint>();
		try (Stream<String> stream = Files.lines(Path.of(fileName))) {
	        stream.forEach(dbFile::interpretLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return dbFile;
	}
	
	private void interpretLine(String line)
	{
		String[] val = line.split("=");
		if (val.length < 2) {
			return;
		}
		
		DataPoint dp = DataPoint.fromDBLine(line);
		this.dataPoints.add(dp);
	}
	
	public ArrayList<DataPoint> getDataPoints()
	{
		return this.dataPoints;
	}
}