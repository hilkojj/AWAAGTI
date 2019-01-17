import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class WorkerThread implements Runnable {
    private Socket connection;
    private final static boolean SHOW_DEBUG = true;
    private final static String EXPORT_PATH = "../db_exports";
    private final static String DATA_PATH = "../db_testdata";
    private final static String DATA_EXTENSION = "txt";
    private final static boolean CACHE = false;

    public WorkerThread(Socket connection) { this.connection = connection; }
    private BufferedReader conReader = null;
    private BufferedWriter conWriter = null;

    @Override
    public void run() {
        log("Worker thread started");


        try {
            char s;
            conReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            conWriter = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            String options = "stations: 1234,1235\n" +
                    "from: 12469548968\n" +
                    "to: 23848273958\n" +
                    "interval: 1\n" +
                    "what: temperature\n" +
                    "sortBy: temperature\n" +
                    "limit: 10\n" +
                    "filter:temp,<,10\n";
            process(new Options(options));

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
    private void process(Options options) throws IOException {

        String fileName = options.getFileName();
        File tmpFile = new File(EXPORT_PATH+"/"+fileName);
        conWriter.write(fileName);

        if(tmpFile.exists() && CACHE == true) {
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
    private BufferedWriter createFile(Options options) {
        String fileName = options.getFileName();
        log("Creating file: "+fileName);
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
    private void moveFile(Options options) {
        String fileName = options.getFileName();
        log("Finished export: "+fileName);
        File exportReadyFile = new File(fileName);
        exportReadyFile.renameTo(new File(EXPORT_PATH+"/"+fileName));
    }


    /*
        Loop all files we need
     */
    private void collectData(BufferedWriter writer, Options options) {
        try {
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<export>\n");

            for (File file : getDataFiles(options)) {
                log("Collecting from: " +file.getName());
                collectDatePoint(file, writer, options);
            }

            writer.write("</export>\n");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private File[] getDataFiles(Options options) {   // TODO: Based on options
        File dir = new File(DATA_PATH);
        File[] directoryListing = dir.listFiles(file -> file.getName().substring(file.getName().lastIndexOf(".")).contains(DATA_EXTENSION));
        if (directoryListing != null)
            return directoryListing;

        log("No usable data found in the database at:" + DATA_PATH);
        return new File[0];
    }

    /*
        Collect all data from a single file
     */
    private void collectDatePoint(File file, BufferedWriter writer, Options options) throws IOException {

        ArrayList<StationData> stations = getStations(file, options);
        if (stations.size() > 0) {

            writer.write("\t<datepoint date=”???” time=”???”>\n");
            writer.write("\t\t<stations>\n");

            for (StationData station : stations)
                collectStation(station, writer, options);

            writer.write("\t\t</stations>\n");
            writer.write("\t</datepoint>\n");
        }
    }

    private ArrayList<StationData> getStations(File file, Options options) {
        ArrayList<StationData> list = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            //TODO: <====== ADD EXTREME SPEED
//            StringBuilder str = new StringBuilder();
////            br.skip(61*19); // TODO: first jump to position
//
//            char c = '?';
//
//            for(int i=0; i<60; i++) {
//                c = (char) br.read();
//                if(c == '#') {
//                    br.readLine(); // TODO use skip
//                    i += 60;       // TODO use skip
//                } else
//                    str.append(c);
//
//            }

            String str = "";
            while (true) {

                str = br.readLine();
                if (str == null)
                    break;
                StationData s = new StationData(br.readLine());
                if (IntStream.of(options.stations).anyMatch(x -> x == s.id))
                    list.add(s);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /*
        Collect all station data we need from a row
     */
    private void collectStation(StationData station, BufferedWriter writer, Options options) throws IOException {
        writer.write("\t\t\t<station id="+station.id+">\n");

            if(options.inSelect("temp"))
                writer.write("\t\t\t\t<temp>"+station.temp+"</temp>\n");

        writer.write("\t\t\t</station>\n");
    }

    /*
        Helper function
     */
    private static void log(Object o) {
        if (SHOW_DEBUG) {
            System.err.print(new java.util.Date());
            System.err.print(": \t");
            System.err.println(o);
        }
    }


    class StationData  {
        public int id = 999;
        public String temp = "?";

        public StationData(String str) {
            id = Integer.parseInt(str.substring(0, str.indexOf("=")));
//            System.out.print(str);
//            System.out.println(id);

            temp = str.substring(str.indexOf("=")+1, str.indexOf(",")); // TODO the others
        }
    }


    class Options {
        private final static String FILE_NAME = "export_";
        private final static String FILE_EXTENSION = "xml";


        private String optionString = "";

        public int[] stations = {7950, 7953, 7873};


        public Options(String options) {
            optionString = options;
//            stations = ...   TODO: get the options from the client
        }

        public boolean inSelect(String str) {
            return true; // TODO: implement this
        }

        private String getFileName() {
            int optionHash = Arrays.hashCode(optionString.toCharArray());
            return FILE_NAME + optionHash +"."+ FILE_EXTENSION;
        }
    }
}