package DBReader;

import shared.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

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
    private String sortBy = "";
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
                    case "sortBy": sortBy = data; break;
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

    // TEST Helper
    public static boolean assertQuery(Iterable it, boolean print, int value) {
        int i = 0;
        Iterator iterator = it.iterator();
        while(iterator.hasNext()) {
            i++;
            if (print)
                System.out.println(iterator.next());
            else
                iterator.next();
        }

        System.out.println(i + " == " + value);
        return (i == value);
    }

    // TEST
    public static void main(String[] args) {
        try {
            boolean DEBUG = false;

//            new Query("limit=10;stations=1234,1356;from=23423423;sortBy=32432432;to=3453454353;interval=1;\n");
//            System.out.println("Syntax: 1");
//
//            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;sortBy=32432432;limit=10;filter=temp,>,-1;\n");
//            System.out.println("Syntax: 2");
//
//            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,sfgfdgd;sortBy=32432432;limit=10;filter=temp,<,10\n");
//            System.out.println("Syntax: 3");
//
//
//            Query q1 = new Query("stations=1234,1356;\n");
//            System.out.println("PARSE: 1 " + assertQuery(q1.getDataFilesNormal(), DEBUG, 5));
//
//            Query q2 = new Query("stations=50,7950;from=0;to=-1\n");
//            System.out.println("PARSE: 2 " + assertQuery(q2.getDataFilesNormal(), DEBUG, 5));
//
//            Query q3 = new Query("stations=50,7950;from=1548348440;to=1548348442\n");
//            System.out.println("PARSE: 3 " + assertQuery(q3.getDataFilesNormal(), DEBUG, 3));
//
//            Query q4 = new Query("stations=50,7950;from=1548348442;to=1548348442;filter=temp,<,-999\n");
//            System.out.println("PARSE: 4 " + assertQuery(q4.getDataFilesNormal(), DEBUG, 1));
//
//
//            Query qs1 = new Query("stations=50,7950;sortedBy=temp;limit=1\n");
//            System.out.println("PARSE SORTED: 1 " + assertQuery(qs1.getDataFilesSorted(), DEBUG, 5));

            Query qs2 = new Query("stations=50,7950;from=1548348440;to=1548348442;sortedBy=temp;limit=1\n");
            System.out.println("PARSE SORTED: 1 " + qs2.getDataFilesSorted().iterator().hasNext());

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
            long cur = from;
            long iteration = 0;
            File nextVal = null;
            String type = "temp_min.txt";

            private int get2pair(long value, int nth) {
                return (int) ((value / (int) Math.pow(10, 8 - (nth * 2))) % 100);
            }

//          1548348440
            public ArrayList<DataPoint> sortMagic(long from, long to) {
                ArrayList<DataPoint> list = new ArrayList<>();
                System.out.println(get2pair(from, 4));

                for(int a=get2pair(from, 4); a%100 != 0; a++) {
//                    String filename = Settings.DATA_PATH + "/" + cur + "/" + type;
//                    File file = new File(filename);
//                    if (file.exists()) {
//                        try {
//                            DBFile dbFile = DBFile.read(file, DataPoint.SummaryType.TEMP);
//                            list.addAll(dbFile.getDataPoints());
//                        } catch (IOException e) { Logger.error(e.getMessage()); e.printStackTrace(); }
//                    }
                    cur += 1;
                    System.out.println(from +" "+ cur +" "+ to);

                }
                return list;
            }

            @Override
            public boolean hasNext() {
                if (iteration >= limit)
                    return false;

                System.out.println(sortMagic(from, to));


                return false;
            }

            public File next() {
                if(nextVal == null)
                    throw new NoSuchElementException();

                iteration ++;
                return nextVal;
            }
        };
    }


    public ArrayList<DataPoint> getStations(File file) {
        ArrayList<DataPoint> list = new ArrayList<>();

        try {
            DBFile dbFile = DBFile.read(file, null, stations, this.filter);
            return dbFile.getDataPoints();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }


    public boolean inSelect(String temp) { // TODO:
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
