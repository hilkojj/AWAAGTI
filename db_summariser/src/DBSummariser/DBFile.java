package DBSummariser;

import java.io.IOException;
import java.nio.file.Files;
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
	
	public void read(String fileName)
	{
		try (Stream<String> stream = Files.lines(Path.get(fileName))) {
	        stream.forEach(this::interpretLine);
		}
	}
	
	private void interpretLine(String line)
	{
		line = line.split("#")[0];
		
		String[] val = line.split("=");
		if (val.length < 2) {
			return;
		}
		
		
	}
}