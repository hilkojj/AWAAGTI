package DBReader;

import java.net.ServerSocket;
import java.net.Socket;


/**
 * This is the DBReader:
 * It is responsible for answering queries from the user.
 * For every query request a new connection has to be setup.
 * You can not trust that the connection will still be up when this code has finished a export.
 *
 * The database data is provided by the shared.DBFile and shared.DataPoint
 *
 * @author Timo
 *
 */
public class Server
{
    public static final int PORT = 12345;

    public static void main(String[] args)
    {
        Socket connection;
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.err.println("db_reader startedi lol");

            while (true) {
                connection = server.accept();
                System.err.println("New connection accepted. handing it over to worker thread");
                Thread worker = new Thread(new WorkerThread(connection));
                worker.start();
            }
        }

        catch (java.io.IOException ioe) { System.err.print("\n\nIOException\n\n" + ioe.getMessage()); }
    }
}