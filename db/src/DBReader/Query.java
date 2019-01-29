package DBReader;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import shared.DBFile;
import shared.DataPoint;
import shared.Logger;
import shared.Settings;

public class Query {
    private final static String FILE_NAME = "export_";
    private final static String FILE_EXTENSION = "xml";
    private final static String FILE_NAME_FORMAT = "yyyyMMdd_HHmmss";
    private final static SimpleDateFormat FILE_FORMATTER = new SimpleDateFormat(FILE_NAME_FORMAT);


    public int hash = -1;

    private int[] stations = {};
    private long from = 1500000000;
    private long to = -1;
    private int interval = 1;
    private ArrayList<String> what = new ArrayList<>();
    private String sortBy = "temperature";
    private boolean distinct = false;
    private int limit = -1;
    private QueryFilter filter;

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
//                    case "distinct": distinct = false; break;
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

        if (to == -1)
            to = System.currentTimeMillis() / 1000;

        if (limit == -1)
            limit = Integer.MAX_VALUE;
    }

    // TEST
    public static void main(String[] args) {
        try {
            new Query("limit=10;stations=1234,1356;from=23423423;sortBy=32432432;to=3453454353;interval=1;\n");
            System.out.println("Syntax: 1");

            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;sortBy=32432432;limit=10;filter=temp,>,-1;\n");
            System.out.println("Syntax: 2");

            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n");
            System.out.println("Syntax: 3");



            Query q1 = new Query("stations=1234,1356;\n");
            int i = 0;
            Iterator<File> iterator1 = q1.getDataFilesNormal().iterator();
            while(iterator1.hasNext()) {
                i++;
            }

            System.out.println("PARSE: 1 " + (i == 5) + " _ " + i);


            Query q2 = new Query("stations=50,7950;from=0;to=-1\n");
            int i2 = 0;
            Iterator<File> iterator2 = q2.getDataFilesNormal().iterator();
            while(iterator2.hasNext()) {
                i2++;
                System.out.println(iterator2.next());
            }

            System.out.println("PARSE: 2 " + (i2 == 5) + " _ " + i2 );


            Query q3 = new Query("stations=50,7950;from=1548348440;to=1548348442\n");
            int i3 = 0;
            Iterator<File> iterator3 = q3.getDataFilesNormal().iterator();
            while(iterator3.hasNext()) {
                i3++;
                System.out.println(iterator3.next());
            }

            System.out.println("PARSE: 3 " + (i3 == 3) + " _ " + i3 );


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public String getFileName() {
        return FILE_NAME + hash +"."+ FILE_EXTENSION;
    }


    public Iterable<File> getDataFiles() {
        try {
            if (sortBy == null)
                return getDataFilesNormal();
            else
                return getDataFilesSorted();

        } catch (Exception e) {
            e.printStackTrace();
            return (Iterable<File>) Collections.emptyIterator();
        }
    }

    /*
        Answer Queries iteratively over all the data
     */
    private Iterable<File> getDataFilesNormal() {
        return () -> new Iterator<File>() {
            long cur = from;
            long iteration = 0;
            File nextVal = null;
            String currentPath = "";

            @Override
            public boolean hasNext() {
                if (iteration >= limit)
                    return false;

                while (cur <= to) {
                    if (!findDir()) return false;

                    String filename = Settings.DATA_PATH + "/" + currentPath.replaceAll("(.{2})", "$1/") + "/" + cur + ".txt";

                    nextVal = new File(filename);
                    cur += interval;
                    if (nextVal.exists()) return true;
                }

                return false;
            }

            @Override
            public File next() {
                if(nextVal == null)
                    throw new NoSuchElementException();

                iteration ++;
                return nextVal;
            }

            private boolean findDir() {
                String timestampStr = "" + cur;
                if (currentPath.length() == 8 && timestampStr.startsWith(currentPath)) return true;
                goDeeper:
                while (currentPath.length() / 2 < 4 || !timestampStr.startsWith(currentPath)) {

                    if (timestampStr.startsWith(currentPath)) {

                        String currentPathWithSlashes = Settings.DATA_PATH + "/" + currentPath.replaceAll("(.{2})", "$1/");
//                        System.out.println(currentPathWithSlashes);

                        String[] directories = new File(currentPathWithSlashes).list();

                        for (String dir : directories) {
                            String path = currentPath + dir;
                            if (timestampStr.startsWith(path)) {
                                currentPath = path;

                                if (currentPath.length() == 8) return true; // dir was found, cannot go any deeper ;-(

                                continue goDeeper; // dir was found, eg: 15/48 was found, now find 15/48/34
                            }
                        }
                        // no dir found

                        int antiDeepness = 4 - currentPath.length() / 2;
                        long minimalTimestamp = cur + (int) Math.pow(100, antiDeepness);
                        while (cur < minimalTimestamp) cur += interval;

                        return findDir();
                    } else currentPath = currentPath.substring(0, currentPath.length() - 2);

                }
                return false;
            }
        };
    }

    /*
        Answer to Queries with index results
     */
    private Iterable<File> getDataFilesSorted() {
        return () -> new Iterator<File>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public File next() {
                if(!hasNext())
                    throw new NoSuchElementException();

                return null;
            }
        };
    }


    public ArrayList<DataPoint> getStations(File file) {
        ArrayList<DataPoint> list = new ArrayList<>();

        try {
            DBFile dbFile = DBFile.readFile(file, null, stations, this.filter);
            return dbFile.getDataPoints();
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