package DBReader;

import shared.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class WorkerThread implements Runnable
{
    private Socket connection;

    public WorkerThread(Socket connection) throws SocketException { this.connection = connection; connection.setKeepAlive(Settings.KEEP_SOCKETS_ALIVE); }
    private BufferedReader conReader = null;
    private ConWriter conWriter = null;
    private int progress = 0;

    @Override
    public void run()
    {
        Logger.log("Worker thread started");

        try {
            conReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            conWriter = new ConWriter(new OutputStreamWriter(connection.getOutputStream()));

            try {
                process(new Query(conReader.readLine()));
            } catch (Exception e) {
                Logger.error(e.getMessage());
                e.printStackTrace();
                conWriter.write(ConWriter.Types.error, e.getMessage());
            }

            System.out.println("NEEEE");

            conReader.close();
            conWriter.close();
            connection.close();

        } catch (IOException e) { e.printStackTrace(); /*We are done just ignore connection from now on*/ }
    }


    /*
        Start doing what the user requested to us.
     */
    private void process(Query query)
    {
        String fileName = query.getFileName();
        File tmpFile = new File(Settings.EXPORT_PATH+"/"+fileName);
        conWriter.write(ConWriter.Types.file, fileName);

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
    private BufferedWriter createFile(Query query)
    {
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
    private void moveFile(Query query)
    {
        String fileName = query.getFileName();
        Logger.log("Finished export: "+fileName);
        File exportReadyFile = new File(fileName);
        exportReadyFile.renameTo(new File(Settings.EXPORT_PATH+"/"+fileName));
    }


    /*
        Loop through all files we need
     */
    private void collectData(BufferedWriter writer, Query query)
    {
        try {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<export>\n");

            int oldProgress = 0;

            for (File file : query.getDataFiles()) {
                int newProgress = query.progress();
                if (oldProgress < newProgress) {
                    oldProgress = newProgress;
                    conWriter.write(ConWriter.Types.progress, ""+newProgress);
                }

//                Logger.log("Collecting from: " +file.getName());
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
    private void collectDatePoint(File file, BufferedWriter writer, Query query) throws IOException
    {
        ArrayList<DataPoint> stations = query.getStations(file);
        if (stations.size() > 0) {

            writer.write("\t<datepoint time=\""+file.getName().split("\\.")[0]+"\">\n"); // TODO: date=”???” time=”???”
            writer.write("\t\t<stations>\n");

            for (DataPoint station : stations) {
                collectStation(station, writer, query);
            }

            writer.write("\t\t</stations>\n");
            writer.write("\t</datepoint>\n");
        }
    }

    /*
        Collect all station data we need from a row
     */
    public void collectStation(DataPoint station, BufferedWriter writer, Query query) throws IOException
    {
        writer.write("\t\t\t<station id=\""+station.clientID+"\">\n");

        for (DBValue e : query.getSelection())
            if (query.inSelect(e))
                writer.write("\t\t\t\t<"+e.toString()+">"+station.temp+"</"+e.toString()+">\n"); // TODO: get selected

//        if(query.is)
//            writer.write("\t\t\t\t<when>"+station.temp+"</temp>\n");

        writer.write("\t\t\t</station>\n");
    }


}