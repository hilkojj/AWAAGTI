package shared;


import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
	
		
	private byte[] dbLine;

	DecimalFormat df = new DecimalFormat("#.#");
	
	public enum SummaryType {
		TEMP
	}

	public static DataPoint fromLine(byte[] line)
	{
		return fromDBLine(line, null);
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
	public byte[] makeDBLine()
	{
		if (this.dbLine == null) {
			if (this.summaryType != null) {
				this.dbLine = new byte[5+8];
				switch (this.summaryType) {
				case TEMP:
					short temp = (short) (this.temp+100);
					
					this.dbLine[3] = (byte)(temp >> 8);
					this.dbLine[4] = (byte)(temp);
					break;
				default:
					System.out.println("ERROR: invalid summaryType: " + this.summaryType);
				}
				
				if (this.summaryDateTime != null) {
					// RIP 2038
					int uts = (int) this.summaryDateTime.toEpochSecond(ZoneOffset.UTC);
					
					this.dbLine[5] = (byte)(uts >>> 24);
					this.dbLine[6] = (byte)(uts >>> 16);
					this.dbLine[7] = (byte)(uts >>> 8);
					this.dbLine[8] = (byte)uts;
				}
			} else {
				//this.dbLine = String.format("%d=%.01f", this.clientID, ((float)this.temp)/10);
				
				this.dbLine = new byte[5];
				
				short temp = (short) (this.temp+100);
				
				this.dbLine[3] = (byte)((short)temp >> 8);
				this.dbLine[4] = (byte)((short)temp);
			}
			
			this.dbLine[0] = (byte)((short)this.clientID >> 16);
			this.dbLine[1] = (byte)((short)this.clientID >> 8);
			this.dbLine[2] = (byte)((short)this.clientID);
		}
		
		return this.dbLine;
	}
	
	public static DataPoint fromDBLine(byte[] line, SummaryType summaryType)
	{
		DataPoint dp = new DataPoint();
		
		if (line.length < 3) {
			System.out.println("ERROR: incorrect dbLine in fromDBLine. Less than 3");
			return null;
		}
		
		dp.clientID = ((line[0] & 0xff) << 16) | ((line[1] & 0xff) << 8) | (line[2] & 0xff);
		
		//System.out.println("ARGS" + String.join(", ", args) + " " + line);
		
		if (summaryType == null) {
			if (line.length < 5) {
				System.out.println("ERROR: incorrect dbLine in fromDBLine. Less than 5");
				System.out.println(line);
				for (int i = 0; i < line.length ;i++) {
					System.out.println(line[i]);
				}
				return null;
			}
			// Regular DB file, not a summary file.
			dp.temp = ((line[3] << 8) | (line[4]))-100;
			return dp;
		}
		
		switch (summaryType) {
		case TEMP:
			dp.temp = ((line[3] << 8) | (line[4]))-100;
			break;
		default:
			System.out.println("ERROR: unknown summaryType in fromDBLine: " + summaryType);
		}
		
		long uts = line[5] << 24 | (line[6] & 0xFF) << 16 | (line[7] & 0xFF) << 8 | (line[8] & 0xFF);
		
		dp.summaryDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(uts), ZoneId.of("UTC"));
		
		dp.summaryType = summaryType;
		
		return dp;
	}
	
	public int getVal(SummaryType sType)
	{
		switch(sType) {
		case TEMP:
			return this.temp;
		default:
			return 0;
		}
	}
}
