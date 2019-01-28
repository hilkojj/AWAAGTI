public class speedtest {

	public static void main(String[] args) {
		double testding[] = {11, 2, 3, 4, 5, 6, 7, 8,45 ,4, 2, 4, 5 ,6, 4, 2, 3, 5, 65, 6, 4, 3,4 ,6, 6, 43, 999, 2, 3, 4, 5, 6, 7, 8,45 ,4, 2, 4, 5 ,6, 4, 2, 3, 5, 65, 6, 4, 3,4 ,6, 6, 43, 999};
		long oldtime = System.currentTimeMillis();

		for (int i = 0; i < 1000000; i++) {
			test2(testding);
		}
		System.out.println(System.currentTimeMillis()-oldtime);

	}
	public static void test1(double testding[]) {

		int sum = 0;
		for (int i = 5; i < testding.length; i++) {

			for (int j = i-5; j < i; j++) {
				sum += testding[j];
			}
			double avg = 1.0d*sum/5;
			sum = 0;
			if (avg > testding[i]) {
				testding[i] = avg;
			}
		}
	}

	public static void test2(double testding[]) {

		double sum = 0;
		for (int i = 30; i < testding.length; i++) {

			for (int j = i-30; j < i; j++) {
				sum += testding[j];
			}
			double avg = 1.0d*sum/30;
			sum = 0;
			if (avg*1.2 > testding[i] || avg/1.2 < testding[i]) {
				testding[i] = avg;
			}
		}
	}
}
