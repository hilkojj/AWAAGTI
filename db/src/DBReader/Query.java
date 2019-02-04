package DBReader;

import shared.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static DBReader.DBHelper.getNth2pair;
import static DBReader.DBHelper.timestampToFolder;

/**
 * Parse a Query and provide Iterators to loop the data.
 *
 * @author Timo
 *
 */
public class Query
{
    private final static String FILE_NAME = "export_";
    private final static String FILE_EXTENSION = "xml";

    private int hash = -1;
    ArrayList<String> parseWarnings = new ArrayList<>(); // Package private

    // FOR TESTING PURPOSES THESE VARIABLES ARE PUBLIC TO THIS PACKAGE
    int[] stations = {};
    long from = 0;
    long cur = from;
    long first = -1;
    long to = -1;
    int interval = 1;
    ArrayList<DBValue> what = new ArrayList<>();
    String sortBy = "";
    int limit = -1;
    QueryFilter filter = new QueryFilter();


    public Query(String options) throws Exception
    {
        try {
            options = options.replace("\t","").replace("\n","").replace(" ", "");
            for (String line : options.split(";")) {
                if(line.length() < 3)
                    continue;

                String data = line.substring(line.indexOf("=") + 1);

                switch (line.substring(0, line.indexOf("="))) {
                    case "stations":    stations = Stream.of( data.split(",") ).map(Integer::parseInt).mapToInt(i->i).sorted().toArray(); break;
                    case "from":        from     = correctTime(data, 0); cur = from; break;
                    case "to":          to       = correctTime(data, System.currentTimeMillis() / 1000); break;
                    case "interval":    interval = Integer.parseInt(data) ; break;
                    case "sortBy":      sortBy   = data; break;
                    case "limit":       limit    = Integer.parseInt(data); break;
                    case "filter":      filter   = new QueryFilter(data); break;
                    case "what":
                        if (data.equals("*"))
                            what.addAll(Arrays.asList(DBValue.values()));
                        else
                            for (String s : data.split(","))
                                what.add(DBValue.valueOf(s.toUpperCase()));
                        break;
                    default:
                        parseWarnings.add("throw new NotImplementedException(): " + line);
                        Logger.log(parseWarnings.get(parseWarnings.size() -1));
                }
            }
            hash = Arrays.hashCode( (Arrays.toString(stations) + from + to + interval + sortBy + limit + filter.getOriginalInput() + Arrays.toString(what.toArray()) ).toCharArray());
        }
        catch (Exception e) {
            Logger.error(e.getMessage());
            throw new Exception("Your query does not have the proper syntax");
        }


        // Programming mistakes
        if (sortBy.length() > 0 && !sortBy.contains("_"))
            throw new Exception("You are testing sortby wrong");


        // Corrections
        if (to == -1)
            to = System.currentTimeMillis() / 1000;

        if (limit == -1)
            limit = Integer.MAX_VALUE;


        // Not what the user is expecting
        if (what.size() == 0)
            parseWarnings.add("You did not select what data you would like back from the query. By default you only select the ID.");

        if (isIndexedQuery() && limit > stations.length)
            parseWarnings.add("Please note that all Indexed sortby queries are distinct.");

        if ((sortBy.length() > 0 && what.size() >= 2) || !Collections.disjoint(Arrays.asList(sortBy.split("_")), what))
            throw new Exception("Sorted Index Queries do not support non indexed selections");


        // Tell the client about wrong queries
        if (stations.length == 0)
            throw new Exception("You did not Select which stations you want");

        if(interval > 1 && sortBy.length() > 0)
            throw new Exception("Interval > 1 does not have any meaning when processing a sorted query.");

        if(to < from)
            throw new Exception("to < from does not have any meaning." + to + " < " + from);
    }

    private long correctTime(String dataStr, long orDefault) {
        long data = Long.parseLong(dataStr);
        if (dataStr.length() > 10)
            data /= 1000;

        return (data <= -1)? orDefault : data;
    }

    public ArrayList<DBValue> getWhat() {
        return what;
    }

    public String getFileName()
    {
        return FILE_NAME + hash +"."+ FILE_EXTENSION;
    }


    /**
     * @return Iterator that loops all Files that pass the Query rules
     */
    public Iterable<File> getDataFiles()
    {
        try {
            if (isIndexedQuery())
                return getDataFilesSorted();
            else
                return getDataFilesNormal();

        } catch (Exception e) {
            e.printStackTrace();
            return (Iterable<File>) Collections.emptyIterator();
        }
    }

    /**
     * Answer Queries iteratively over all the data
     *
     * @return result files
     */
    private Iterable<File> getDataFilesNormal()
    {
        return () -> new Iterator<>() {
            long iteration = 0;
            File nextVal = null;
            String currentPath = "";

            @Override
            public boolean hasNext() {
                if (iteration >= limit)
                    return false;

                while (cur < to) {
                    if (!findDir()) return false;

                    String filename = (timestampToFolder(currentPath) + "/" + cur + "." + Settings.DATA_EXTENSION).replace("//", "/");
//                    Logger.log("_FOUND_:" + filename);

                    nextVal = new File(filename);
                    cur += interval;
                    if (first == -1) first = cur;
                    if (nextVal.exists()) return true;
                }

                return false;
            }

            @Override
            public File next() {
                if (nextVal == null)
                    throw new NoSuchElementException();

                iteration++;
                return nextVal;
            }

            private boolean findDir() {
                searchForTheDir:
                while (true) {

                    String timestampStr = "" + cur;
                    if (currentPath.length() == 8 && timestampStr.startsWith(currentPath)) return true;
                    goDeeper:
                    while (currentPath.length() / 2 < 4 || !timestampStr.startsWith(currentPath)) {

                        if (timestampStr.startsWith(currentPath)) {

                            String currentPathWithSlashes = Settings.DATA_PATH + "/" + currentPath.replaceAll("(.{2})", "$1/");
//                            Logger.log(currentPathWithSlashes);

                            String[] directories = new File(currentPathWithSlashes).list();

                            for (String dir : directories) {
                                String path = currentPath + dir;
                                if (timestampStr.startsWith(path)) {
                                    currentPath = path;

                                    if (currentPath.length() == 8)
                                        return true; // dir was found, cannot go any deeper ;-(

                                    continue goDeeper; // dir was found, eg: 15/48 was found, now find 15/48/34
                                }
                            }
                            // no dir found

                            int antiDeepness = 4 - currentPath.length() / 2;
                            long minimalTimestamp = cur + (int) Math.pow(100, antiDeepness);


                            if (cur < minimalTimestamp)
                                cur += Math.ceil((minimalTimestamp - cur) / (float) interval) * interval;

                            if (cur > to)
                                return false;

                            continue searchForTheDir;

                        } else currentPath = currentPath.substring(0, currentPath.length() - 2);
                    }
                }
            }
        };
    }


    /**
     * Answer to Queries with index results
     *
     * @return 1 result file
     */
    private Iterable<File> getDataFilesSorted()
    {
        return () -> new Iterator<>() {
            String summaryFileName = sortBy + "_sum";
            Boolean hasExported = false;
            DBValue summaryType = DBValue.TEMP;

            /*
                Get the query answer by combining index files
             */
            private ArrayList<DataPoint> getIndexResults() {
                hasExported = true;
                Map<Integer, DataPoint> indexResultMap = new HashMap<>();

                for (int n = 4; n >= 0; n--) { // /15/48/34/85/ file.txt loop every depth [0-4]
                    for (int a = getNth2pair(from, n); a % 100 != 0 && cur < to; a++) // loop from cur to 00 until we surpass TO
                        processCurPosition(n, indexResultMap);

                    if (cur > to) { // We are to far we need to jump back and slowly go back down in depth
                        cur -= distanceToNextCur(n);
                        break;
                    }
                }

                for (int n = 0; n <= 4; n++)   // /15/48/34/85/ file.txt loop every depth [0-4]
                    for (int a = getNth2pair(cur, n); a != getNth2pair(to, n) && cur < to; a++) // Go from 00 towards TO
                        processCurPosition(n, indexResultMap);

                return new ArrayList<>(indexResultMap.values());
            }


            /*
                Get the current file or summary based on the cur variable and process it based on the depth given
             */
            private void processCurPosition(int n, Map<Integer, DataPoint> indexResultMap) {
                String filename = timestampToFolder(cur);
                while (filename.endsWith("00/"))
                    filename = filename.substring(0, filename.length() - 3);

                filename = (n == 4) ? filename + cur + "." + Settings.DATA_EXTENSION : filename + summaryFileName + "." + Settings.DATA_EXTENSION;
                filename = filename.replace("//", "/");
                File file = new File(filename);
//                Logger.log(n + " " + from + " " + cur + " " + to + " - " + filename);

                cur += distanceToNextCur(n);

                if (file.exists()) {
                    Logger.log("_FOUND_:" + filename);
                    if (first == -1)
                        first = cur;
                    if (cur <= to)
                        updateIndexResult(indexResultMap, getAllDataPointsFromFile(file, (n == 4) ? null : summaryType));
                }
            }

            /*
                How far do we need to increment based on the depth
             */
            private long distanceToNextCur(int n) {
                return (long) Math.pow(10, (4 - n) * 2);
            }

            /*
                This updates our index results based on new DataPoint's
             */
            private void updateIndexResult(Map<Integer, DataPoint> indexResult, ArrayList<DataPoint> newItems) {
                for (DataPoint dp : newItems) {
                    indexResult.putIfAbsent(dp.getClientID(), dp);
                    DataPoint old = indexResult.get(dp.getClientID());

                    switch (sortBy) {
                        case "temp_min":
                            if (old.getTemp() < dp.getTemp()) {
//                                Logger.log("UPDATE: " + old.temp + " < " +dp.temp);
                                indexResult.put(dp.getClientID(), dp);
                            }
                            break;
                        case "temp_max":
                            if (old.getTemp() > dp.getTemp()) {
//                                Logger.log("UPDATE: " + old.temp + " > " +dp.temp);
                                indexResult.put(dp.getClientID(), dp);
                            }
                            break;
                    }
                }
            }

            // Exceptions take up a lot of code
            private ArrayList<DataPoint> getAllDataPointsFromFile(File file, DBValue type) {
                try {
                    if (type == null) {
                        DBFile dbFile = DBFile.read(file);
                        Logger.error(dbFile.getDataPoints().size());
                        ArrayList<DataPoint> list = dbFile.getDataPoints();
                        for (DataPoint dp : list) {
                            dp.setSummaryType(summaryType);
                            Logger.log(summaryType + " ==== " + cur);
                            dp.setSummaryDateTime(cur);
                        }
                    } else {
                        DBFile dbFile = DBFile.read(file, type);
//                        dbFile.getDataPoints().forEach(x -> System.out.print(x.getTemp() + " "));
                        return dbFile.getDataPoints();
                    }
                } catch (IOException e) {
                    Logger.error(e.getMessage());
                    e.printStackTrace();
                }

                return new ArrayList<>();
            }


            @Override
            public boolean hasNext() {
                return !hasExported;
            }

            public File next() {
                if (hasExported)
                    throw new NoSuchElementException();

                ArrayList<DataPoint> dps = filterResult(sortResult(getIndexResults()));
                hasExported = true;

                DBFile dbFile = new DBFile();
                String fileName = Settings.DATA_PATH + "/sortedQuery_cache_" + hash + "." + Settings.DATA_EXTENSION;
                dbFile.setFileName(fileName.replace("//", "/"));

                if (dps.size() > limit)
                    dbFile.setDataPoints(new ArrayList<>(dps.subList(0, limit)));
                else if (dps.size() != 0)
                    dbFile.setDataPoints(dps);
//                else
//                    throw new Exception("No data was found.");   // TODO: NO DATA WAS FOUND

                Logger.log("SAVED: " + fileName + " \t rows:" + dps.size());

//                for (DataPoint dp : dps)
//                    Logger.log(dp.getClientID() + " " + dp.getTemp() + " " + dp.getSummaryDateTime());

                try {
                    dbFile.write();
                } catch (IOException e) {
                    Logger.error("Our sorted index query has not been saved");
                    e.printStackTrace();
                }

                return new File(fileName);
            }

            private ArrayList<DataPoint> sortResult(ArrayList<DataPoint> dps) {
                if (sortBy.contains("temp"))
                    dps.sort(Comparator.comparingInt(DataPoint::getTemp));
                else if (sortBy.contains("wind"))
                    dps.sort(Comparator.comparingInt(DataPoint::getWindSpeed));
                else
                    Logger.error("We have made a parsing error.");

                if (sortBy.contains("max"))
                    Collections.reverse(dps);

                return dps;
            }

            private ArrayList<DataPoint> filterResult(ArrayList<DataPoint> dps) {
                List<Integer> stationsList = Arrays.stream(stations).boxed().collect(Collectors.toList());
                ArrayList<DataPoint> newList = new ArrayList<>();
                for (DataPoint dp : dps)
                    if (stationsList.contains(dp.getClientID()))
                        newList.add(dp);

                return newList;
            }
        };
    }


    /**
     * @param file the file
     * @return gives all DataPoints from file
     */
    public ArrayList<DataPoint> getStations(File file) {
        ArrayList<DataPoint> list = new ArrayList<>();

        try {
            DBValue summaryType = isIndexedQuery()? DBValue.TEMP : null;
            DBFile dbFile = DBFile.read(file, summaryType, stations, this.filter);
            return dbFile.getDataPoints();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * @return if sortBy is used in Query
     */
    public boolean isIndexedQuery()
    {
        return sortBy != null && sortBy.length() > 0;
    }

    /**
     * @param select what kind of value you have
     * @return if user whats it.
     */
    public boolean inSelect(DBValue select) {
        return what.contains(select);
    }

    /**
     * The percentage that is returned may have windows like behavior.
     * @return [1-100] as a percentage
     */
    public int progress()
    {
        if(first == -1)
            return 1;

        long diffFromStart = cur - first;
        long total = to - first;
        float proc = ((diffFromStart / (float)total) * 100);
//        Logger.log(from + " " + cur + " " + to + " = "+proc);
//        Logger.log(diffFromStart + " " + total + " = "+proc);
        return (int) Math.min(proc, 100);
    }
}
