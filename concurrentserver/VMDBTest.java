public class VMDBTest
{
	public static void main(String[] args)
	{
		VMDB v;
		try {
			v = new VMDB("127.0.0.1", 12345);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		while (true) {
			System.out.println("Send");
			v.sendBegin("aaa", "aaa");
			for (int i = 0; i < 8000; i++) {
				v.sendDataPoint(i+1000, (float)i/10);
			}
			v.sendEnd();
		}
	}
}
