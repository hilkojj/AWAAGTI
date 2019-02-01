package shared;


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

	private DBValue summaryType;
	private long summaryDateTime;

	
	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	public int getTemp() {
		return temp;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public DBValue getSummaryType() {
		return summaryType;
	}
	public void setSummaryType(DBValue summaryType) {
		this.summaryType = summaryType;
	}

	public long getSummaryDateTime() {
		return summaryDateTime;
	}
	public void setSummaryDateTime(long summaryDateTime) {
		this.summaryDateTime = summaryDateTime;
	}


	private byte[] dbLine;

	DecimalFormat df = new DecimalFormat("#.#");


	public DataPoint(){ }
	public DataPoint(String proof, int clientID, int temp) {
		if(proof.equals("JUST FOR TESTING")) {
			this.clientID = clientID;
			this.temp = temp;
		} else { Logger.error("This is just for testing"); }
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
	 * makeDBLine formats the DataPoint to a byte array in the format of
	 * the .awaagti database files.
	 * 
	 * View the provided documentation on the binary file format.
	 * 
	 * @return byte[] binary data for this datapoint
	 */
	public byte[] makeDBLine()
	{
		if (this.dbLine == null) {
			if (this.summaryType != null) {
				this.dbLine = new byte[5+8];
				switch (this.summaryType) {
				case TEMP:
					int temp = (this.temp+100);
					
					this.dbLine[3] = (byte)(temp >> 8);
					this.dbLine[4] = (byte)(temp);
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
				this.dbLine = new byte[5];
				
				int temp = this.temp+100;
				
				this.dbLine[3] = (byte)(temp >> 8);
				this.dbLine[4] = (byte)(temp);
			}
			
			this.dbLine[0] = (byte)(this.clientID >> 16);
			this.dbLine[1] = (byte)(this.clientID >> 8);
			this.dbLine[2] = (byte)(this.clientID);
		}
		
		return this.dbLine;
	}
	
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
		
		dp.summaryDateTime = uts;
		
		dp.summaryType = summaryType;
		
		return dp;
	}
	
	public int getVal(DBValue sType)
	{
		switch(sType) {
		case TEMP:
			return this.temp;
		default:
			return 0;
		}
	}
}
