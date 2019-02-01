package shared;

class DataPointTest
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
		dp3.setSummaryType(DBValue.TEMP);

		dbLine = dp3.makeDBLine();
		
		DataPoint dp4 = DataPoint.fromDBLine(dbLine, DBValue.TEMP);

		a(dp3.getClientID() == dp4.getClientID());
		System.out.println(dp3.getTemp());
		System.out.println(dp4.getTemp());
		a(dp3.getTemp() == dp4.getTemp());
		a(dp3.getSummaryType() == dp4.getSummaryType());
		
		DataPoint dp5 = new DataPoint();

		dp5.setClientID(5);
		dp5.setTemp(215);
		dp5.setSummaryType(DBValue.TEMP);
		dp5.setSummaryDateTime(1548596433);

		dbLine = dp5.makeDBLine();
		
		DataPoint dp6 = DataPoint.fromDBLine(dbLine, DBValue.TEMP);

		a(dp5.getClientID() == dp6.getClientID());
		System.out.println(dp5.getTemp());
		System.out.println(dp6.getTemp());
		a(dp5.getTemp() == dp6.getTemp());
		a(dp5.getSummaryType() == dp6.getSummaryType());	
		a(dp5.getSummaryDateTime() == dp6.getSummaryDateTime());
		
		
		DataPoint dp7 = new DataPoint();
		dp7.setWindSpeed(100);
		dp7.setTemp(-20);
		
		DataPoint dp8 = DataPoint.fromDBLine(dp7.makeDBLine(), null);
		a(dp7.getTemp() == dp8.getTemp());
		a(dp7.getWindSpeed() == dp8.getWindSpeed());
		a(dp7.getSummaryType() == dp8.getSummaryType());	
		a(dp7.getSummaryDateTime() == dp8.getSummaryDateTime());
		
		DataPoint dp9 = new DataPoint();
		dp9.setWindSpeed(100);
		dp9.setTemp(-20);
		dp9.setSummaryType(DBValue.WIND);
		dp9.setSummaryDateTime(1548596433);
		
		DataPoint dp10 = DataPoint.fromDBLine(dp9.makeDBLine(), DBValue.WIND);
		a(dp9.getTemp() != dp10.getTemp());
		System.out.println(dp9.getWindSpeed());
		System.out.println(dp10.getWindSpeed());
		a(dp9.getWindSpeed() == dp10.getWindSpeed());
		a(dp9.getSummaryType() == dp10.getSummaryType());	
		a(dp9.getSummaryDateTime() == dp10.getSummaryDateTime());
	}
	
	public static void a(boolean b) throws Exception
	{
		if (!b) {
			throw new Exception("doesn't assert");
		}
	}
}
