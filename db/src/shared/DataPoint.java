package shared;

/**
 * DataPoint is a single state of a specific weather station.
 * It stores the weather station ID and the measured values.
 * 
 * Both regular and summary (from summary files) DataPoints are supported.
 * Refer to the documentation regarding the .awaagti file format.
 * 
 * It implements Comparable to sort a collection of DataPoints on the
 * client (=weather station) ID.
 * 
 * @author remi
 */
public class DataPoint implements Comparable<DataPoint>
{
	private int clientID;
	private int temp = -1;
	private int windSpeed = -1;

	// Only if summary.
	private DBValue summaryType;
	private long summaryDateTime;

	private byte[] dbLine;

	public DataPoint()
	{ }

	public DataPoint(String proof, int clientID, int temp)
	{
		if(proof.equals("JUST FOR TESTING")) {
			this.clientID = clientID;
			this.temp = temp;
		} else { Logger.error("This is just for testing"); }
	}

	/**
	 * fromLine interprets a chunk in a regular (non-summary) DBFile
	 * and gives the resulting DataPoint.
	 * 
	 * @param line
	 * @return
	 */
	public static DataPoint fromLine(byte[] line)
	{
		return fromDBLine(line, null);
	}

	@Override
	public int compareTo(DataPoint dp2) {
		return this.clientID - dp2.clientID;
	}
	
	/**
	 * makeDBLine formats the DataPoint to a byte array in the format of
	 * the .awaagti database files.
	 * 
	 * View the provided documentation on the binary file format.
	 * 
	 * @return byte[] binary data for this datapoint
	 */
	public byte[] makeDBLine()
	{
		if (this.dbLine != null) {
			return this.dbLine;
		}
		
		if (this.summaryType != null) {
			this.dbLine = new byte[5+8];
			switch (this.summaryType) {
			case TEMP:
				int temp = (this.temp+1000);
				if (temp < 0) {
					temp = 0;
				}
				
				this.dbLine[3] = (byte)(temp >> 8);
				this.dbLine[4] = (byte)(temp);
				break;
			case WIND:
				int windSpeed = this.windSpeed;
				if (windSpeed >= 255) {
					windSpeed = 255;
				}

				this.dbLine[3] = (byte)windSpeed;
				break;
			default:
				System.out.println("ERROR: invalid summaryType: " + this.summaryType);
			}
			
			if (this.summaryDateTime != 0) {
				// RIP 2038
				int uts = (int) this.summaryDateTime;
				
				this.dbLine[5] = (byte)(uts >>> 24);
				this.dbLine[6] = (byte)(uts >>> 16);
				this.dbLine[7] = (byte)(uts >>> 8);
				this.dbLine[8] = (byte)uts;
			}
		} else {
			this.dbLine = new byte[6];
			
			int temp = this.temp+1000;
			if (temp < 0) {
				temp = 0;
			}
			
			this.dbLine[3] = (byte)(temp >> 8);
			this.dbLine[4] = (byte)(temp);
			
			int windSpeed = this.windSpeed;
			if (windSpeed >= 255) {
				windSpeed = 255;
			}
			
			this.dbLine[5] = (byte)windSpeed;
		}
		
		this.dbLine[0] = (byte)(this.clientID >> 16);
		this.dbLine[1] = (byte)(this.clientID >> 8);
		this.dbLine[2] = (byte)(this.clientID);
		
		return this.dbLine;
	}
	
	/**
	 * fromDBLine interprets a chunk in a DBFile and gives the resulting DataPoint.
	 * Optionally, a summaryType can be specified if the chunk is of a summary file,
	 * and not a regular dbfile.
	 * 
	 * @param line
	 * @param summaryType optional
	 * @return
	 */
	public static DataPoint fromDBLine(byte[] line, DBValue summaryType)
	{
		DataPoint dp = new DataPoint();
		
		if (line.length < 3) {
			Logger.log("ERROR: incorrect dbLine in fromDBLine. Less than 3");
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
			//dp.temp = (((line[3] & 0xff) << 8) | (line[4] & 0xff))-100;
			dp.temp = (((line[3]) << 8) | (line[4] & 0xff))-1000;
			
			if (line.length < 6) {
				return dp;
			}
			
			dp.windSpeed = (line[5] & 0xff);

			return dp;
		}
		
		switch (summaryType) {
		case TEMP:
			dp.temp = (((line[3] & 0xff) << 8) | (line[4] & 0xff))-1000;
			break;
		case WIND:
			dp.windSpeed = line[3] & 0xff;
			break;
		default:
			System.out.println("ERROR: unknown summaryType in fromDBLine: " + summaryType);
		}
		
		long uts = (line[5] & 0xff) << 24 | (line[6] & 0xFF) << 16 | (line[7] & 0xFF) << 8 | (line[8] & 0xFF);
		
		dp.summaryDateTime = uts;
		
		dp.summaryType = summaryType;
		
		return dp;
	}
	
	/**
	 * getVal returns a measured weather station value of this
	 * DataPoint for the given DBValue.
	 * 
	 * @param sType
	 * @return
	 */
	public int getVal(DBValue sType)
	{
		switch(sType) {
		case TEMP:
			return this.temp;
		case WIND:
			return this.windSpeed;
		default:
			return 0;
		}
	}

	/**
	 * getValFormatted formats and returns a measured weather station
	 * value of this DataPoint for the given DBValue.
	 * 
	 * @param sType
	 * @return
	 */
	public String getValFormatted(DBValue sType)
	{
		switch(sType) {
		case TEMP:
			return String.format("%.01f", (float)(this.temp)/10);
		case WIND:
			return String.format("%.01f", (float)(this.windSpeed)/10);
		default:
			return "";
		}
	}
	
	/**
	 * setVal sets the value of the given DBValue to the given int.
	 * 
	 * @param val
	 * @param sType
	 */
	public void setVal(int val, DBValue sType)
	{
		switch (sType) {
		case TEMP:
			this.temp = val;
			break;
		case WIND:
			this.windSpeed = val;
			break;
		default:
			Logger.error("DBValue not implemented");
		}
	}

	public int getWindSpeed()
	{
		return windSpeed;
	}

	public void setWindSpeed(int windSpeed)
	{
		this.windSpeed = windSpeed;
	}
	
	public int getClientID()
	{
		return clientID;
	}

	public void setClientID(int clientID)
	{
		this.clientID = clientID;
	}

	public int getTemp()
	{
		return temp;
	}

	public void setTemp(int temp)
	{
		this.temp = temp;
	}

	public DBValue getSummaryType()
	{
		return summaryType;
	}

	public void setSummaryType(DBValue summaryType)
	{
		this.summaryType = summaryType;
	}

	public long getSummaryDateTime()
	{
		return summaryDateTime;
	}

	public void setSummaryDateTime(long summaryDateTime)
	{
		this.summaryDateTime = summaryDateTime;
	}
	
	public String toString()
	{
		return String.format("%s: (%s %d) %d %d", this.clientID, this.summaryType, this.summaryDateTime, this.temp, this.windSpeed);
	}
}