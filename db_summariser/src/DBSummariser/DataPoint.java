package DBSummariser;

import java.text.DecimalFormat;

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
	
	public String[] summary;

	private String dbLine;

	DecimalFormat df = new DecimalFormat("#.#");

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
			if (this.summary != null) {
				this.dbLine = String.format("%d=%s", this.clientID, String.join(",", this.summary));
			} else {
				this.dbLine = String.format("%d=%.01f", this.clientID, ((float)this.temp)/10);
			}
		}
		
		return this.dbLine;
	}
	
	public static DataPoint fromDBLine(String line)
	{
		DataPoint dp = new DataPoint();
		String[] args = dp.parse(line);
		
		float temp =  Float.parseFloat(args[0]);
		dp.temp = (int) (temp*10);
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
