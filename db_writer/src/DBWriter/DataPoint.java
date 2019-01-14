package DBWriter;

public class DataPoint implements Comparable<DataPoint>
{
	public int clientID;
	public int temp;

	@Override
	public int compareTo(DataPoint dp2) {
		return this.clientID - dp2.clientID;
	}
}
