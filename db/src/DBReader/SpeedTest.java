package DBReader;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Date;
import java.util.stream.Stream;

public class SpeedTest {

    public static void main(String[] args){
//        new SpeedTest();
        System.out.println( new Date().getTime() / 1000 );
    }

    public static final int SKIP = 100;

    public SpeedTest() {
        String url = "data/test.txt";
        int MAX = 1000;

        long startTime = 0;

        startTime = System.currentTimeMillis();
        for (int i=0; i < MAX; i++)
            test1(url);
        System.out.println("\n\n "+ (System.currentTimeMillis() - startTime) + "\n\n");

        startTime = System.currentTimeMillis();
        for (int i=0; i < MAX; i++)
            test2(url);
        System.out.println("\n\n "+ (System.currentTimeMillis() - startTime) + "\n\n");

        startTime = System.currentTimeMillis();
        for (int i=0; i < MAX; i++)
            test3(url);
        System.out.println("\n\n "+ (System.currentTimeMillis() - startTime) + "\n\n");

        startTime = System.currentTimeMillis();
        for (int i=0; i < MAX; i++)
            test4(url);
        System.out.println("\n\n "+ (System.currentTimeMillis() - startTime) + "\n\n");
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

            if(line.equals("100=-3.4,84.3,-34.2,-61.6,-51.7,-6.3,-92.9,37.8,-74.7,58.8,##########"))
                System.out.print(".");

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
            br.skip(SKIP*71);
            for(int i=0; i<69; i++)
                str.append((char)br.read());

            if(str.toString().equals("100=-3.4,84.3,-34.2,-61.6,-51.7,-6.3,-92.9,37.8,-74.7,58.8,##########"))
                System.out.print(".");

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
            br.skip(SKIP*71);

            if(br.readLine().equals("100=-3.4,84.3,-34.2,-61.6,-51.7,-6.3,-92.9,37.8,-74.7,58.8,##########"))
                System.out.print(".");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*
        BufferedReader readline
     */
    private void test4(String url) {
        File file = new File(url);

        try {
            FileInputStream br = new FileInputStream(file);
            StringBuilder str = new StringBuilder();
            br.skip(SKIP*71);
            for(int i=0; i<69; i++)
                str.append((char)br.read());

            if(str.toString().equals("100=-3.4,84.3,-34.2,-61.6,-51.7,-6.3,-92.9,37.8,-74.7,58.8,##########"))
                System.out.print(".");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
