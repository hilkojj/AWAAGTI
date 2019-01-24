package DBReader;

import shared.DataPoint;
import shared.Logger;
import shared.Settings;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class WorkerThread implements Runnable {
    private Socket connection;

    public WorkerThread(Socket connection) throws SocketException { this.connection = connection; connection.setKeepAlive(Settings.KEEP_SOCKETS_ALIVE); }
    private BufferedReader conReader = null;
    private BufferedWriter conWriter = null;

    @Override
    public void run() {
        Logger.log("Worker thread started");

        try {
            conReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            conWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

//            String query = "stations=1234,1235\n" +
//                    "from=12469548968\n" +
//                    "to=23848273958\n" +
//                    "interval=1\n" +
//                    "what=temperature\n" +
//                    "sortBy=temperature\n" +
//                    "limit=10\n" +
//                    "filter=temp,<,10\n";

            try {
                process(new Query(conReader.readLine()));
            } catch (Exception e) {
                conWriter.write(e.getMessage());
            }

            conReader.close();
            conWriter.close();
            connection.close();

        } catch (IOException e) { /* e.printStackTrace(); We are done just ignore connection from now on*/ }
    }


    /*
        Start doing what the user requested to us.
     */
    private void process(Query query) throws IOException {

        String fileName = query.getFileName();
        File tmpFile = new File(Settings.EXPORT_PATH+"/"+fileName);
        conWriter.write(fileName);

        if(tmpFile.exists() && Settings.CACHE == true) {
            Logger.log("Cached request: "+fileName);
        } else { // We only create files if there is not already one when cache is enabled
            BufferedWriter writer = createFile(query);
            collectData(writer, query);
            moveFile(query);
        }
    }

    /*
        Create the file + hash
     */
    private BufferedWriter createFile(Query query) {
        String fileName = query.getFileName();
        Logger.log("Creating file: "+fileName);
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
        This moves the file to the exports directory
     */
    private void moveFile(Query query) {
        String fileName = query.getFileName();
        Logger.log("Finished export: "+fileName);
        File exportReadyFile = new File(fileName);
        exportReadyFile.renameTo(new File(Settings.EXPORT_PATH+"/"+fileName));
    }


    /*
        Loop through all files we need
     */
    private void collectData(BufferedWriter writer, Query query) {
        try {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<export>\n");

            for (File file : query.getDataFiles(query)) {
                Logger.log("Collecting from: " +file.getName());
                collectDatePoint(file, writer, query);
            }

            writer.write("</export>\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Collect all data from a single file
     */
    private void collectDatePoint(File file, BufferedWriter writer, Query query) throws IOException {

        ArrayList<DataPoint> stations = query.getStations(file, query);
        if (stations.size() > 0) {


//            new SimpleDateFormat("dd-MM-yyyy").format(date);
//            new SimpleDateFormat("HH:mm:ss").format(date);

            writer.write("\t<datepoint time=\""+file.getName().split("\\.")[0]+"\">\n"); // TODO: date=”???” time=”???”
            writer.write("\t\t<stations>\n");

            for (DataPoint station : stations)
                collectStation(station, writer, query);

            writer.write("\t\t</stations>\n");
            writer.write("\t</datepoint>\n");
        }
    }

    /*
    Collect all station data we need from a row
 */
    public void collectStation(DataPoint station, BufferedWriter writer, Query query) throws IOException {
        writer.write("\t\t\t<station id=\""+station.clientID+"\">\n");

        if(query.inSelect("temperature"))
            writer.write("\t\t\t\t<temp>"+station.temp+"</temp>\n");

        writer.write("\t\t\t</station>\n");
    }
}