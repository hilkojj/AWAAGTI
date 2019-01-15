import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;

public class SpeedTest {

    public static void main(String[] args) {
        new SpeedTest();
    }

    public SpeedTest() {
        String url = "../db_testdata/20190114_143202.txt";
        int MAX = 10000;
        int MAXavg = 100;

        long startTime = 0;
        long avgTime = 0;
        for(int a=0; a < MAXavg; a++) {
            startTime = System.currentTimeMillis();
            for (int i=0; i < MAX; i++)
                test2(url);
            avgTime += System.currentTimeMillis() - startTime;
        }
        long endTime = avgTime / MAXavg;

//        test2(url);

        System.out.println(endTime);
    }

    /*
        java 9 Streams
     */
    private void test1(String url) {
        File file = new File(url);
//        System.out.println(file.length());

        String line;
        try (Stream<String> lines = Files.lines(file.toPath())) {
            line = lines.skip(1).findFirst().get();
            System.out.println(line);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        BufferedReader read
     */
    private void test2(String url) {
        File file = new File(url);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder str = new StringBuilder();
            br.skip(13);
            for(int i=0; i<12; i++)
                str.append((char)br.read());
            System.out.println(str);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
        BufferedReader readline
     */
    private void test3(String url) {
        File file = new File(url);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            br.skip(13);
            System.out.println(br.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
