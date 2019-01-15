import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class WorkerThread implements Runnable {
    private Socket connection;
    private final static boolean SHOW_DEBUG = false;


    public WorkerThread(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        print("Worker thread started\n");


        try {
            String s;
            BufferedReader bin = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((s = bin.readLine()) != null) {
                print(s);
            }
            bin.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void print(Object o) {
        if (SHOW_DEBUG)
            System.out.println(o);
    }
}