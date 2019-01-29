package DBReader;

import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static final int PORT = 12345;

    public static void main(String[] args) {
//        System.out.println("Working Directory = " +
//                System.getProperty("user.dir"));
        Socket connection;
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.err.println("db_reader started");

            while (true) {
                connection = server.accept();
                System.err.println("New connection accepted. handing it over to worker thread");
                Thread worker = new Thread(new WorkerThread(connection));
                worker.start();
            }
        }

        catch (java.io.IOException ioe) { System.err.print("IOException: " + ioe.getMessage()); }
    }
}