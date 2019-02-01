package shared;

public class DataPointTest
{
	public static void main(String[] args) throws Exception
	{
		DataPoint dp = new DataPoint();
		dp.clientID = 13377;
		dp.temp = -19;
		
		byte[] dbLine = dp.makeDBLine();
		
		for (int i = 0; i < dbLine.length; i++) {
			System.out.println(dbLine[i]);
		}
		
		DataPoint dp2 = DataPoint.fromLine(dbLine);
		
		System.out.println(dp.clientID);
		System.out.println(dp2.clientID);
		a(dp.clientID == dp2.clientID);
		a(dp.temp == dp2.temp);
		
		
		DataPoint dp3 = new DataPoint();
		dp3.clientID = 5;
		dp3.temp = 215;
		dp3.summaryType = DataPoint.SummaryType.TEMP;

		dbLine = dp3.makeDBLine();
		
		DataPoint dp4 = DataPoint.fromDBLine(dbLine, DataPoint.SummaryType.TEMP);

		a(dp3.clientID == dp4.clientID);
		System.out.println(dp3.temp);
		System.out.println(dp4.temp);
		a(dp3.temp == dp4.temp);
		a(dp3.summaryType == dp4.summaryType);
		
		DataPoint dp5 = new DataPoint();
		dp5.clientID = 5;
		dp5.temp = 215;
		dp5.summaryType = DataPoint.SummaryType.TEMP;
		dp5.summaryDateTime = 1548596433;

		dbLine = dp5.makeDBLine();
		
		DataPoint dp6 = DataPoint.fromDBLine(dbLine, DataPoint.SummaryType.TEMP);

		a(dp5.clientID == dp6.clientID);
		System.out.println(dp5.temp);
		System.out.println(dp6.temp);
		a(dp5.temp == dp6.temp);
		a(dp5.summaryType == dp6.summaryType);	
		System.out.println(dp5.summaryDateTime);
		System.out.println(dp6.summaryDateTime);
		a(dp5.summaryDateTime == dp6.summaryDateTime);
	}
	
	public static void a(boolean b) throws Exception
	{
		if (!b) {
			throw new Exception("doesn't assert");
		}
	}
}
