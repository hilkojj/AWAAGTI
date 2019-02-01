package shared;

public class DataPointTest
{
	public static void main(String[] args) throws Exception
	{
		DataPoint dp = new DataPoint();
		dp.setClientID(13377);
		dp.setTemp(-19);
		
		byte[] dbLine = dp.makeDBLine();
		
		for (int i = 0; i < dbLine.length; i++) {
			System.out.println(dbLine[i]);
		}
		
		DataPoint dp2 = DataPoint.fromLine(dbLine);
		
		System.out.println(dp.getClientID());
		System.out.println(dp2.getClientID());
		a(dp.getClientID() == dp2.getClientID());
		a(dp.getTemp() == dp2.getTemp());
		
		
		DataPoint dp3 = new DataPoint();
		dp3.setClientID(5);
		dp3.setTemp(215);
		dp3.setSummaryType(DataPoint.SummaryType.TEMP);

		dbLine = dp3.makeDBLine();
		
		DataPoint dp4 = DataPoint.fromDBLine(dbLine, DataPoint.SummaryType.TEMP);

		a(dp3.getClientID() == dp4.getClientID());
		System.out.println(dp3.getTemp());
		System.out.println(dp4.getTemp());
		a(dp3.getTemp() == dp4.getTemp());
		a(dp3.getSummaryType() == dp4.getSummaryType());
		
		DataPoint dp5 = new DataPoint();
		dp5.setClientID(5);
		dp5.setTemp(215);
		dp5.setSummaryType(DataPoint.SummaryType.TEMP);
		dp5.setSummaryDateTime(1548596433);

		dbLine = dp5.makeDBLine();
		
		DataPoint dp6 = DataPoint.fromDBLine(dbLine, DataPoint.SummaryType.TEMP);

		a(dp5.getClientID() == dp6.getClientID());
		System.out.println(dp5.getTemp());
		System.out.println(dp6.getTemp());
		a(dp5.getTemp() == dp6.getTemp());
		a(dp5.getSummaryType() == dp6.getSummaryType());	
		a(dp5.getSummaryDateTime() == dp6.getSummaryDateTime());
	}
	
	public static void a(boolean b) throws Exception
	{
		if (!b) {
			throw new Exception("doesn't assert");
		}
	}
}
