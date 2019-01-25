package DBReader;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import shared.DataPoint;
import shared.Logger;
import shared.Settings;

public class Query {
    private final static String FILE_NAME = "export_";
    private final static String FILE_EXTENSION = "xml";
    private final static String FILE_NAME_FORMAT = "yyyyMMdd_HHmmss";
    private final static SimpleDateFormat FILE_FORMATTER = new SimpleDateFormat(FILE_NAME_FORMAT);


    public int hash = -1;

    public int[] stations = {};
    public long from = 0;
    public long to = 0;
    public int interval = 1;
    public ArrayList<String> what = new ArrayList<>();
    public String sortBy = "temperature";
    public int limit = 10;
    public QueryFilter filter;

    public Query(String options) throws Exception {
        try {
            hash = Arrays.hashCode(options.toCharArray());
            for (String line : options.split(";")) {
                if(line.length() < 3)
                    continue;

                String data = line.substring(line.indexOf("=") + 1).replace("\n", "" );

                switch (line.substring(0, line.indexOf("="))) {
                    case "stations":  stations = Stream.of( data.split(",") ).map(Integer::parseInt).mapToInt(i->i).toArray(); break;
                    case "from":  from = Long.parseLong(data); break;
                    case "to": to = Long.parseLong(data); break;
                    case "interval": interval = Integer.parseInt(data); break;
                    case "what":  what.addAll(Arrays.asList(data.split(","))); break;
//                    case "sortBy": sortBy = data; break;
                    case "limit": limit = Integer.parseInt(data); break;
                    case "filter": this.filter = new QueryFilter(data); break;
                    default:
                        System.out.println("throw new NotImplementedException(): " + line); // TODO:
                }
            }
        }
        catch (Exception e) {
            Logger.error(e.getMessage());
            throw new Exception("Your query does not have the proper syntax");
        }
    }

    // TEST
    public static void main(String[] args) {
        try {
            Query q1 = new Query("stations=1234,1356;\n");
            AtomicInteger length = new AtomicInteger();
            q1.getDataFiles().forEach(x -> length.addAndGet(1));
            System.out.println("1 " + (length.get() == 4));

            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;sortBy=32432432;limit=10;filter=temp,>,-1;\n");
            System.out.println("2");

            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n");
            System.out.println("3");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getFileName() {
        return FILE_NAME + hash +"."+ FILE_EXTENSION;
    }


    public Iterable<File> getDataFiles() {
        return () -> {
            try {
                return new Iterator<File>() {
                    long start = from;
                    long iteration = 0;
                    File next_val = null;

                    @Override
                    public boolean hasNext() {
                        if (iteration >= limit)
                            return false;

                        while (start < to && next_val != null) {
                            next_val = new File(Settings.DATA_PATH + "/" + start);
                            start += interval;
                        }

                        return start < to;
                    }

                    @Override
                    public File next() {
                        if(next_val == null || iteration >= limit)
                            throw new NoSuchElementException();

                        iteration ++;
                        return next_val;
                    }
                };
            } catch (Exception e) {
                e.printStackTrace();
                return Collections.emptyIterator();
            }
        };
    }


    public ArrayList<DataPoint> getStations(File file) {
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
                if (IntStream.of(stations).anyMatch(x -> x == s.clientID))
                	//if (this.filter.compare(s)) {
                		list.add(s);
                	//}
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

    private boolean accept(File file) {
        try {
            long time = FILE_FORMATTER.parse(file.getName()).getTime() / 1000;

//                System.out.print("FROM " + from + " < " + time + " && " + time + " > " + to + " = ");
//                System.out.println((from < time && time > to) == false);

            if (to == -1)
                return (from < time);

            return (from < time && time > to) == false;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return file.getName().substring(file.getName().lastIndexOf(".")).contains(Settings.DATA_EXTENSION);
    }
}