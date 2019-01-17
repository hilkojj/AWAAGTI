import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class WorkerThread implements Runnable {
    private Socket connection;
    private final static boolean SHOW_DEBUG = true;
    private final static String FILE_NAME = "export_";
    private final static String FILE_EXTENSION = "xml";
    private final static String EXPORT_PATH = "../db_exports";


    public WorkerThread(Socket connection) { this.connection = connection; }
    BufferedReader conReader = null;
    BufferedWriter conWriter = null;

    @Override
    public void run() {
        log("Worker thread started");


        try {
            char s;
            conReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            conWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            String options = "TODO"; // TODO: get the options from the client
            process(options);

            while ((s = (char)conReader.read()) != '\t') {
                System.err.print(s);
            }
            conReader.close();
            conWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Start doing what the user requested to us.
     */
    private void process(String options) throws IOException {

        String fileName = getFileName(options);
        File tmpFile = new File(EXPORT_PATH+"/"+fileName);
        conWriter.write(fileName);

        if(tmpFile.exists()) {
            log("Cached request: "+fileName);
        } else { // We only create files if there is not already one
            BufferedWriter writer = createFile(options);
            collectData(writer, options);
            moveFile(options);
        }
    }

    /*
        Create the file + hash
     */
    private BufferedWriter createFile(String options) {
        String fileName = getFileName(options);
        log("Creating file: "+fileName);
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getFileName(String options) {
        int optionHash = Arrays.hashCode(options.toCharArray());
        return FILE_NAME + optionHash +"."+ FILE_EXTENSION;
    }

    /*
        This moves the file to the exports directory
     */
    private void moveFile(String options) {
        String fileName = getFileName(options);
        log("Finished export: "+fileName);
        File exportReadyFile = new File(fileName);
        exportReadyFile.renameTo(new File(EXPORT_PATH+"/"+fileName));
    }


    /*
        Loop all files we need
     */
    private void collectData(BufferedWriter writer, String options) {
        try {
            writer.write("<export>\n");
            // TODO LOOP DATAPOINTS
                collectDatePoint(writer, options);

            writer.write("</export>\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Collect all data from a single file
     */
    private void collectDatePoint(BufferedWriter writer, String options) throws IOException {
        writer.write("<datepoint date=”2019-01-01” time=”15:55:56”>\n");
            writer.write("<stations>\n");
            // TODO LOOP STATIONS
                collectStation(writer, options);
            writer.write("</stations>\n");
        writer.write("</datepoint>\n");
    }

    /*
        Collect all station data we need from a row
     */
    private void collectStation(BufferedWriter writer, String options) throws IOException {
        writer.write("<station id=32123>\n");
            writer.write("<temp>-60.1</temp>\n");
        writer.write("</station>\n");
    }

    /*
        Helper function
     */
    private static void log(Object o) {
        if (SHOW_DEBUG) {
            System.out.print(new java.util.Date());
            System.out.print(": \t");
            System.out.println(o);
        }
    }
}