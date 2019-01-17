package DBWriter;

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
			this.dbLine = String.format("%d=%.01f", this.clientID, ((float)this.temp)/10);
		}
		
		return this.dbLine;
	}
}