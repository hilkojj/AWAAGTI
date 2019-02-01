package DBReader;

import shared.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    private final static String FILE_NAME_FORMAT = "yyyyMMdd_HHmmss";
    private final static SimpleDateFormat FILE_FORMATTER = new SimpleDateFormat(FILE_NAME_FORMAT);

    private int hash = -1;
    String parseWarning = ""; // Package private

    private int[] stations = {};
    private long from = 0;
    private long cur = from;
    private long to = -1;
    private int interval = 1;
    private ArrayList<DBValue> what = new ArrayList<>();
    private String sortBy = "";
    private int limit = -1;
    private QueryFilter filter;


    public Query(String options) throws Exception
    {
        try {
//            hash = Arrays.hashCode(options.toCharArray());  // stupid hash
            for (String line : options.split(";")) {
                if(line.length() < 3)
                    continue;

                String data = line.substring(line.indexOf("=") + 1).replace("\n", "" );

                switch (line.substring(0, line.indexOf("="))) {
                    case "stations":    stations = Stream.of( data.split(",") ).map(Integer::parseInt).mapToInt(i->i).sorted().toArray(); break;
                    case "from":        from = Long.parseLong(data); cur = from; break;
                    case "to":          to = Long.parseLong(data); break;
                    case "interval":    interval = Integer.parseInt(data); break;
                    case "sortBy":      sortBy = data; break;
                    case "limit":       limit = Integer.parseInt(data); break;
                    case "filter":      filter = new QueryFilter(data); break;
                    case "what":
                        if (data.equals("*"))
                            what.addAll(Arrays.asList(DBValue.values()));
                        else
                            for (String s : Arrays.asList(data.split(",")))
                                what.add(DBValue.valueOf(s.toUpperCase()));
                        break;
                    default:
                        parseWarning = "throw new NotImplementedException(): " + line;
                        Logger.log(parseWarning);
                }
            }
            hash = Arrays.hashCode( (Arrays.toString(stations) + from + to + interval + sortBy + limit + filter.originalInput + Arrays.toString(what.toArray()) ).toCharArray());
        }
        catch (Exception e) {
            Logger.error(e.getMessage());
            throw new Exception("Your query does not have the proper syntax");
        }


        // Correct -1 inputs to there meaning
        if (to == -1)
            to = System.currentTimeMillis() / 1000;

        if (limit == -1)
            limit = Integer.MAX_VALUE;

        if (what.size() == 0)
            parseWarning = "You did not select what data you would like back from the query. By default you only select the ID.";


        // Tell the client about wrong queries
        if(interval > 1 && sortBy.length() > 0)
            throw new Exception("Interval > 1 does not have any meaning when processing a sorted query.");

        if(to < from)
            throw new Exception("to < from does not have any meaning.");

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
                return getDataFilesNormal();
            else
                return getDataFilesSorted();

        } catch (Exception e) {
            e.printStackTrace();
            return (Iterable<File>) Collections.emptyIterator();
        }
    }

    /*

     */

    /**
     * Answer Queries iteratively over all the data
     *
     * @return result files
     */
    private Iterable<File> getDataFilesNormal()
    {
        return () -> new Iterator<File>()
        {
            long iteration = 0;
            File nextVal = null;
            String currentPath = "";

            @Override
            public boolean hasNext()
            {
                if (iteration >= limit)
                    return false;

                while (cur < to) {
                    if (!findDir()) return false;

                    String filename = timestampToFolder(currentPath) +"/"+ cur +"."+ Settings.DATA_EXTENSION;
//                    Logger.error(filename);

                    nextVal = new File(filename);
                    cur += interval;
                    if (nextVal.exists()) return true;
                }

                return false;
            }

            @Override
            public File next()
            {
                if(nextVal == null)
                    throw new NoSuchElementException();

                iteration ++;
                return nextVal;
            }

            private boolean findDir()
            {
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

                                    if (currentPath.length() == 8) return true; // dir was found, cannot go any deeper ;-(

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
        return () -> new Iterator<File>()
        {
            String summaryFileName = sortBy+"_sum";
            Boolean hasExported = false;
            DBValue summaryType = DBValue.TEMP;

            /*
                Get the query answer by combining index files
             */
            private ArrayList<DataPoint> getIndexResults()
            {
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
                String filename = (n == 4) ? timestampToFolder(cur/100) + cur +"."+ Settings.DATA_EXTENSION : timestampToFolder(cur/100) + summaryFileName +"."+Settings.DATA_EXTENSION;
                filename = filename.replace("//", "/");
                File file = new File(filename);
//                Logger.log(n + " " + from + " " + cur + " " + to + " - " + filename);

                cur += distanceToNextCur(n);

                if (file.exists()) {
                    Logger.log("_FOUND_:" + filename);
                    if (cur <= to)
                        updateIndexResult(indexResultMap, getAllDataPointsFromFile(file, (n == 4) ? null : summaryType));
                }
            }

            /*
                How far do we need to increment based on the depth
             */
            private long distanceToNextCur(int n) {
                return (long) Math.pow(10, (4-n) * 2);
            }

            /*
                This updates our index results based on new DataPoint's
             */
            private void updateIndexResult(Map<Integer, DataPoint> indexResult, ArrayList<DataPoint> newItems) {
                for (DataPoint dp : newItems) {
                    indexResult.putIfAbsent(dp.clientID, dp);
                    DataPoint old = indexResult.get(dp.clientID);

                    switch (sortBy) {
                        case "temp_min":
                            if (old.temp < dp.temp) {
//                                Logger.log("UPDATE: " + old.temp + " < " +dp.temp);
                                indexResult.put(dp.clientID, dp);
                            }
                            break;
                        case "temp_max":
                            if (old.temp > dp.temp) {
//                                Logger.log("UPDATE: " + old.temp + " > " +dp.temp);
                                indexResult.put(dp.clientID, dp);
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
                        ArrayList<DataPoint> list = dbFile.getDataPoints();
                        for (DataPoint dp : list) {
                            dp.summaryType = summaryType;
                            dp.summaryDateTime = cur;
                        }
                    } else {
                        DBFile dbFile = DBFile.read(file, type);
                        return dbFile.getDataPoints();
                    }
                } catch (IOException e) { Logger.error(e.getMessage()); e.printStackTrace(); }

                return new ArrayList<>();
            }

            @Override
            public boolean hasNext()
            {
                return !hasExported;
            }

            public File next()
            {
                if (hasExported)
                    throw new NoSuchElementException();

                ArrayList<DataPoint> dps = getIndexResults();
                hasExported = true;

                DBFile dbFile = new DBFile();
                String fileName = Settings.DATA_PATH+"/sortedQuery_cache_" + hash +"."+Settings.DATA_EXTENSION;
                dbFile.setFileName(fileName.replace("//", "/")); // todo: file trick

                if (dps.size() > limit)
                    dbFile.setDataPoints(new ArrayList<>(dps.subList(0, limit)));
                else if (dps.size() != 0)
                    dbFile.setDataPoints(dps);
//                else
//                    return new File(fileName); //TODO: NO DATA WAS FOUND

//                Logger.error(fileName);
//                Logger.error(dps.size());

                try {
                    dbFile.write();
                } catch (IOException e) {
                    Logger.error("Our sorted index query has not been saved");
                    e.printStackTrace();
                }

                return new File(fileName);
            }
        };
    }


    public ArrayList<DataPoint> getStations(File file) {
        ArrayList<DataPoint> list = new ArrayList<>();

        try {
//            Logger.error(Arrays.toString(this.stations));
            DBFile dbFile = DBFile.read(file, null, stations, this.filter);
            return dbFile.getDataPoints();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean isIndexedQuery()
    {
        return sortBy == null || sortBy.length() == 0;
    }

    public boolean inSelect(DBValue select) { // TODO: IMPLEMENT WHAT
        return what.contains(select.toString());
    }

    public int progress()
    {
        long diffFromStart = cur - from;
        long total = to - from;
        float proc = ((diffFromStart / (float)total) * 100);
//        Logger.log(from + " " + cur + " " + to + " = "+proc);
//        Logger.log(diffFromStart + " " + total + " = "+proc);
        return (int) Math.min(proc, 100);
    }
}
