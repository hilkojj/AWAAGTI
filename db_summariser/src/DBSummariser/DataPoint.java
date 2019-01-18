package DBSummariser;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * DataPoint is a single state of a specific weather station.
 * It stores the weather station ID and the measured values.
 * 
 * @author remi
 *
 */
public class DataPoint implements Comparable<DataPoint>
{
	public int clientID;
	public int temp;
	
	public SummaryType summaryType;
	public LocalDateTime summaryDateTime;
	
		
	private String dbLine;

	DecimalFormat df = new DecimalFormat("#.#");
	
	private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss");
	private static DateTimeFormatter summaryDateTimeFormatter =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	
	enum SummaryType {
		TEMP
	}

	@Override
	public int compareTo(DataPoint dp2) {
		return this.clientID - dp2.clientID;
	}
	
	/**
	 * makeDBLine formats the DataPoint to a string in the format of
	 * the db_writer database files.
	 * 
	 * The string is formatted as:
	 * {clientID}={temp in always one decimal}
	 * 
	 * @return String Formatted string
	 */
	public String makeDBLine()
	{
		if (this.dbLine == null) {
			if (this.summaryType != null) {
				String val = "";
				switch (this.summaryType) {
				case TEMP:
					val = String.format("%.01f", ((float)this.temp)/10);
					break;
				default:
					System.out.println("ERROR: invalid summaryType: " + this.summaryType);
				}

				this.dbLine = String.format("%d=%s", this.clientID, val);

				if (this.summaryDateTime != null) {
					this.dbLine += "," + this.summaryDateTime.format(dateFormatter) + "," + this.summaryDateTime.format(timeFormatter);
				}
			} else {
				this.dbLine = String.format("%d=%.01f", this.clientID, ((float)this.temp)/10);
			}
		}
		
		return this.dbLine;
	}
	
	public static DataPoint fromDBLine(String line, SummaryType summaryType)
	{
		DataPoint dp = new DataPoint();
		String[] args = dp.parse(line);
		
		if (summaryType == null) {
			float temp =  Float.parseFloat(args[0]);
			dp.temp = (int) (temp*10);
			return dp;
		}
		
		switch (summaryType) {
		case TEMP:
			float temp =  Float.parseFloat(args[0]);
			dp.temp = (int) (temp*10);

			break;
		default:
			System.out.println("ERROR: unknown summaryType in fromDBLine: " + summaryType);
		}
		
		dp.summaryDateTime = LocalDateTime.parse(args[1] + " " + args[2], summaryDateTimeFormatter);
		
		dp.summaryType = summaryType;
		
		return dp;
	}
	
	private String[] parse(String line)
	{
		String[] parts = line.split("=");
		if (parts.length < 2) {
			System.out.println("DataPoint: invalid datapoint line: " + line);
			return null;
		}

		this.clientID = Integer.parseInt(parts[0]);
		
		return parts[1].split(",");
	}
}
