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
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}
