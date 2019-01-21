import java.io.*;
import java.nio.file.Files;
import java.util.stream.Stream;

public class SpeedTest {

    public static void main(String[] args) {
//        new SpeedTest();

        int ding = 1547814209;
    }

    public static final int SKIP = 100;

    public SpeedTest() {
        String url = "../db_testdata/test.txt";
        int MAX = 1000;
        int MAXavg = 1000;

        long startTime = 0;
        long avgTime = 0;
        for(int a=0; a < MAXavg; a++) {
            startTime = System.currentTimeMillis();
            for (int i=0; i < MAX; i++)
                test1(url);
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
            line = lines.skip(SKIP).findFirst().get();
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
            br.skip(SKIP*60);
            for(int i=0; i<59; i++)
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
            br.skip(SKIP*60);
            System.out.println(br.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
