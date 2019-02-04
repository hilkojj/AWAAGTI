package DBReader;

import shared.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Every request gets a Thread.
 *
 * @author Timo
 *
 */
public class WorkerThread implements Runnable
{
    private Socket connection;

    public WorkerThread(Socket connection) throws SocketException { this.connection = connection; connection.setKeepAlive(Settings.KEEP_SOCKETS_ALIVE); }
    private BufferedReader conReader = null;
    private ConWriter conWriter = null;

    @Override
    public void run()
    {
        Logger.log("Worker thread started");

        try {
            conReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            conWriter = new ConWriter(new OutputStreamWriter(connection.getOutputStream()));

            try {
                String options = conReader.readLine();
                Logger.log(options);
                process(new Query(options));
            } catch (Exception e) {
                Logger.error(e.getMessage());
                e.printStackTrace();
                conWriter.write(ConWriter.Types.error, e.getMessage());
            }

            System.out.println("DONE");

            conReader.close();
            conWriter.close();
            connection.close();

        } catch (IOException e) { e.printStackTrace(); /* We are done just ignore the connection from now on */ }
    }


    /*
        Start doing what the user requested to us.
     */
    private void process(Query query)
    {
        for (String warning : query.parseWarnings)
            conWriter.write(ConWriter.Types.error, warning);

        String fileName = query.getFileName();
        File tmpFile = new File(Settings.EXPORT_PATH+"/"+fileName);
        conWriter.write(ConWriter.Types.file, fileName);

        if(tmpFile.exists() && Settings.CACHE == true) {
            Logger.log("Cached request: "+fileName);
        } else { // We only create files if there is not already one when cache is enabled
            BufferedWriter xmlWriter = createFile(query);
            collectData(xmlWriter, query);
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
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
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
    private void collectData(BufferedWriter xmlWriter, Query query)
    {
        try {
            xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmlWriter.write("<export>\n");

            int oldProgress = 0;

            for (File file : query.getDataFiles()) {
                int newProgress = query.progress();
                if (oldProgress < newProgress) {
                    oldProgress = newProgress;
                    conWriter.write(ConWriter.Types.progress, ""+newProgress);
                }

                Logger.log("Collecting from: " +file.getName());
                collectDatePoint(file, xmlWriter, query);
            }

            xmlWriter.write("</export>\n");
            xmlWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Collect all data from a single file
     */
    private void collectDatePoint(File file, BufferedWriter xmlWriter, Query query) throws IOException
    {
        ArrayList<DataPoint> stations = query.getStations(file);
        if (stations.size() > 0) {
            String timestamp = file.getName().replace("sortedQuery_cache_", "" ).split("\\.")[0];

            xmlWriter.write("\t<datepoint time=\""+timestamp+"\" >\n"); // TODO: date=”???” time=”???”
            xmlWriter.write("\t\t<stations>\n");

            for (DataPoint station : stations) {
                collectStation(station, xmlWriter, query);
            }

            xmlWriter.write("\t\t</stations>\n");
            xmlWriter.write("\t</datepoint>\n");
        }
    }

    /*
        Collect all station data we need from a row
     */
    public void collectStation(DataPoint station, BufferedWriter xmlWriter, Query query) throws IOException
    {
        xmlWriter.write("\t\t\t<station id=\""+station.getClientID()+"\">\n");

        for (DBValue e : query.getWhat())
            if (query.inSelect(e))
                xmlWriter.write("\t\t\t\t<"+e.toString().toLowerCase()+">"+station.getValFormatted(e)+"</"+e.toString().toLowerCase()+">\n");

        if(query.isIndexedQuery())
            xmlWriter.write("\t\t\t\t<when>"+station.getSummaryDateTime()+"</when>\n");

        xmlWriter.write("\t\t\t</station>\n");
    }
}
