package DBReader;

import shared.DBFile;
import shared.DataPoint;
import shared.Logger;

import java.io.File;
import java.util.Iterator;


/**
 * Syntax check, parse check normal queries and parse check sorted queries
 *
 * @author Timo
 *
 */
class QueryTest
{
    private final static int FILE_COUNT = 108233;
    private final static boolean DEBUG = false;

    private static int fileCountQuery(Query q)
    {
        int i = 0;
        Iterator iterator = q.getDataFiles().iterator();
        while(iterator.hasNext()) {
            i++;
            if (DEBUG)
                Logger.log(iterator.next());
            else
                iterator.next();
        }

        Logger.log(i);
        return i;
    }

    private static long dataPointCountQuery(Query q) throws Exception {
        long size = 0;
        for (File file : q.getDataFiles()) {
            DBFile dbFile = DBFile.read(file, null, q.stations, q.filter);
            size += dbFile.getDataPoints().size();

            if (DEBUG)
                for (DataPoint dp : dbFile.getDataPoints())
                    Logger.log(dp.toString());
        }
        Logger.log(size);
        return size;
    }

    private static void assertTest(String str, boolean assertTest) {
        if (assertTest == false) {
            Logger.error("Something went wrong");
            Logger.printStacktrace();
            System.exit(-1);
        } else {
            Logger.log(str, Logger.ConsoleColors.GREEN_UNDERLINED);
        }
    }

    public static void main(String[] args)  // THESE ASSERTS DEPEND ON WHAT DATA YOU USE
    {
        try {
            Logger.log("PARSE SYNTAX CHECK:");
            new Query("limit=10;stations=1234,1356;what=temp;from=23423423000;sortBy=temp_min;to=3453454353000;interval=1;\n");
            new Query("stations=1234,1356;from=23423423000;to=3453454353000;interval=1;sortBy=temp_min;limit=10;filter=temp,>,-1;\n");
            new Query("sortBy=temp_max;stations=1234,1356;from=23423423000;to=3453454353000;interval=1;what=temp;limit=10;filter=temp,<,10\n");
            new Query("stations=1234,1356; \t from=23423423000;to=3453454353000; \n interval=1;what=temp;sortBy= wind_max; limit=10;filter=temp, <, 10\n");

            assertTest("PARSE TIMESTAMP: 1 ",  fileCountQuery(new Query("stations=85670;                           ")) == FILE_COUNT);
            assertTest("PARSE TIMESTAMP: 2 ",  fileCountQuery(new Query("stations=85670; from=0;              to=-1")) == FILE_COUNT);
            assertTest("PARSE TIMESTAMP: 3a ", fileCountQuery(new Query("stations=85670; from=1549061102;     to=1549062102")) == 1000);
            assertTest("PARSE TIMESTAMP: 3b ", fileCountQuery(new Query("stations=85670; from=1549061102000;  to=1549062102000")) == 1000);
            assertTest("PARSE TIMESTAMP: 4a ", fileCountQuery(new Query("stations=85670; from=1549061000;     to=1549062000")) == 1000);
            assertTest("PARSE TIMESTAMP: 4b ", fileCountQuery(new Query("stations=85670; from=1549061000000;  to=1549062000000")) == 1000);

            assertTest("PARSE INTERVAL: 1 ", fileCountQuery(new Query("stations=85670;from=1549061000;to=1549062000;interval=1000;")) == 1);
            assertTest("PARSE INTERVAL: 2 ", fileCountQuery(new Query("stations=85670;from=1549061000;to=1549062001;interval=1000;")) == 2);

            assertTest("PARSE LIMIT: 1 ", fileCountQuery(new Query("stations=85670;from=1549061102000;to=1549062102000;limit=100")) == 100); // 1000
            assertTest("PARSE LIMIT: 2 ", fileCountQuery(new Query("stations=85670;from=1549061000;to=1549062001;interval=1000;limit=1")) == 1); // 2

            assertTest("PARSE WHAT: 1 ", fileCountQuery(new Query("stations=85670;")) == FILE_COUNT);
            assertTest("PARSE WHAT: 2 ", fileCountQuery(new Query("stations=85670;what=temp;")) == FILE_COUNT);
            assertTest("PARSE WHAT: 3 ", fileCountQuery(new Query("stations=85670;what=temp,wind;")) == FILE_COUNT);

            assertTest("PARSE SORTED: 1 ", fileCountQuery(new Query("stations=85670;sortBy=temp_min;")) == 1);

            assertTest("PARSE SORTED WHAT: 1 ", fileCountQuery(new Query("stations=85670;sortBy=temp_min;")) == 1);
            assertTest("PARSE SORTED WHAT: 2 ", fileCountQuery(new Query("stations=85670;sortBy=temp_min;what=temp;")) == 1);
            assertTest("PARSE SORTED WHAT: 3 ", fileCountQuery(new Query("stations=85670;sortBy=temp_min;what=temp,wind;")) == 1);

//            Query qs2 = new Query("stations=85670;from=1548690682000;to=1548690699000;sortBy=temp_min;limit=1");
//            File file = qs2.getDataFiles().iterator().next();
//            int size = DBFile.read(file, DBValue.TEMP).getDataPoints().size();
//            Logger.error(size);
//            Logger.log("PARSE SORTED: 2 " + (size == 8000));
//
//            Query qs3 = new Query("stations=50,7950;from=0;to=1548692209000;sortBy=temp_min;");
//            File file3 = qs3.getDataFiles().iterator().next();
//            int works3 = DBFile.read(file3).getDataPoints().size(); // == 8000;
//            Logger.log("PARSE SORTED: 3 " + works3);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
