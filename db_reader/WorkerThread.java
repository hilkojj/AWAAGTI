import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class WorkerThread implements Runnable {
    private Socket connection;
    private final static boolean SHOW_DEBUG = true;
    private final static String FILE_NAME = "export_";


    public WorkerThread(Socket connection) { this.connection = connection; }

    @Override
    public void run() {
        print("Worker thread started\n");


        try {
            char s;
            BufferedReader bin = new BufferedReader(new InputStreamReader(connection.getInputStream()));


            String options = "TODO"; // TODO: get the options from the client

            while ((s = (char)bin.read()) != '\t') {
                print(s);
            }
            bin.close();

            process(options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Start doing what the user requested to us.
     */
    private void process(String options) {
        int optionHash = Arrays.hashCode(options.toCharArray());

        File tmpFile = new File("../db_exports/"+FILE_NAME + optionHash);
        if(tmpFile.exists()) {
            // TODO: The file is already cached
        } else {
            BufferedWriter writer = createFile(optionHash);
            collectData(writer, options);
            // TODO: Move file to db_exports
        }
    }

    /*
        Create the file + hash
     */
    private BufferedWriter createFile(int optionHash) {
        print("Creating file: "+optionHash);
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_NAME + optionHash+".txt"), "utf-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
            writer.write("<stations”>\n");
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
    private static void print(Object o) {
        if (SHOW_DEBUG)
            System.out.println(o);
    }
}