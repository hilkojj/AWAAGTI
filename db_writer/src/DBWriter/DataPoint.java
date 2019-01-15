package DBWriter;

public class DataPoint implements Comparable<DataPoint>
{
	public int clientID;
	public int temp;

	private String dbLine;

	@Override
	public int compareTo(DataPoint dp2) {
		return this.clientID - dp2.clientID;
	}
	
	public String makeDBLine()
	{
		if (this.dbLine == null) {
			this.dbLine = this.clientID + "=" + this.temp;
		}
		
		return this.dbLine;
	}
}