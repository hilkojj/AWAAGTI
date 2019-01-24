package DBReader;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import shared.DataPoint;
import shared.Logger;
import shared.Settings;

public class Query {
    private final static String FILE_NAME = "export_";
    private final static String FILE_EXTENSION = "xml";
    private final static String FILE_NAME_FORMAT = "yyyyMMdd_HHmmss";
    private final static SimpleDateFormat FILE_FORMATTER = new SimpleDateFormat(FILE_NAME_FORMAT);


    public int hash = -1;

    public int[] stations = {50, 4000, 7950};
    public long from = 1547032000;
    public long to = 1547033000;
    public int interval = 1;
    public ArrayList<String> what = new ArrayList<>();
    public String sortBy = "temperature";
    public int limit = 10;
    public QueryFilter filter;

    public Query(String options) throws Exception {
        hash = Arrays.hashCode(options.toCharArray());
        for (String line : options.split("\n")) {
            String data = line.substring(line.indexOf("=")+1);

            switch (line.substring(0, line.indexOf("="))) {
//                case "stations":  stations = Stream.of( data.split(",") ).map(Integer::parseInt).mapToInt(i->i).toArray(); break;
//                case "from":  from = Long.parseLong(data); break;
//                case "to": to = Long.parseLong(data); break;
                case "interval":  interval = Integer.parseInt(data); break;
//                case "what":  what = data; break;
                case "sortBy":  sortBy = data; break;
                case "limit":  limit = Integer.parseInt(data); break;
                case "filter": this.filter = new QueryFilter(data); break;
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
        File[] directoryListing = dir.listFiles(file -> {
            try {
                long time = FILE_FORMATTER.parse(file.getName()).getTime()/1000;

//                System.out.print("FROM " + from + " < " + time + " && " + time + " > " + to + " = ");
//                System.out.println((from < time && time > to) == false);

                return (from < time && time > to) == false;
            } catch (ParseException e) { e.printStackTrace(); }

            return file.getName().substring(file.getName().lastIndexOf(".")).contains(Settings.DATA_EXTENSION);
        });
        if (directoryListing != null)
            return directoryListing;

        Logger.log("No usable data found in the database at:" + Settings.DATA_PATH);
        return new File[0];
    }


    public ArrayList<DataPoint> getStations(File file, Query query) {
        ArrayList<DataPoint> list = new ArrayList<>();

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
                DataPoint s = DataPoint.fromLine(str);
                if (IntStream.of(query.stations).anyMatch(x -> x == s.clientID))
                	if (this.filter.compare(s)) {
                		list.add(s);
                	}
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public boolean inSelect(String temp) {
        return true;
    }
}