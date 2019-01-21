import data.StationData;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Query {
    private final static String FILE_NAME = "export_";
    private final static String FILE_EXTENSION = "xml";

    public int hash = -1;

    public int[] stations = {50, 7950};
    public long from = 0;
    public long to = -1;
    public int interval = 1;
    public String what = "temperature";
    public String sortBy = "temperature";
    public int limit = 10;

    public Query(String options) {
        hash = Arrays.hashCode(options.toCharArray());
        for (String line : options.split("\n")) {
            String data = line.substring(line.indexOf("=")+1);

            switch (line.substring(0, line.indexOf("="))) {
                case "stations":  stations = Stream.of( data.split(",") ).map(Integer::parseInt).mapToInt(i->i).toArray(); break;
                case "from":  from = Long.parseLong(data); break;
                case "to": to = Long.parseLong(data); break;
                case "interval":  interval = Integer.parseInt(data); break;
                case "what":  what = data; break;
                case "sortBy":  sortBy = data; break;
                case "limit":  limit = Integer.parseInt(data); break;
                default:
                    System.out.println("throw new NotImplementedException(): " + line);
            }
        }
    }


    public String getFileName() {
        return FILE_NAME + hash +"."+ FILE_EXTENSION;
    }


    public File[] getDataFiles(Query query) {   // TODO: Based on query
        File dir = new File(Settings.DATA_PATH);
        File[] directoryListing = dir.listFiles(file -> file.getName().substring(file.getName().lastIndexOf(".")).contains(Settings.DATA_EXTENSION));
        if (directoryListing != null)
            return directoryListing;

        Logger.log("No usable data found in the database at:" + Settings.DATA_PATH);
        return new File[0];
    }


    public ArrayList<StationData> getStations(File file, Query query) {
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
//            }

            String str = "";
               while (true) {

                str = br.readLine();
                if (str == null)
                    break;
                StationData s = new StationData(str);
                if (IntStream.of(query.stations).anyMatch(x -> x == s.id))
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
    public void collectStation(StationData station, BufferedWriter writer, Query query) throws IOException {
        writer.write("\t\t\t<station id=\""+station.id+"\">\n");

        if(query.inSelect("temp"))
            writer.write("\t\t\t\t<temp>"+station.temp+"</temp>\n");

        writer.write("\t\t\t</station>\n");
    }

    public boolean inSelect(String temp) {
        return true; // TODO: IMPLEMENT
    }
}