package DBReader;

import shared.DBFile;
import shared.DBValue;
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
    private static boolean assertQuery(Iterable it, boolean print, int value)
    {
        int i = 0;
        Iterator iterator = it.iterator();
        while(iterator.hasNext()) {
            i++;
            if (print)
                Logger.log(iterator.next());
            else
                iterator.next();
        }

        Logger.log(i + " == " + value);
        if (i != value) {
            Logger.error( "Something went wrong");
            Logger.printStacktrace();
            System.exit(-1);
        }

        return (i == value);
    }

    public static void main(String[] args)  // THESE ASSERTS DEPEND ON WHAT DATA YOU USE
    {
        try {
            boolean DEBUG = false;

            Logger.log("PARSE SYNTAX CHECK:");
            new Query("limit=10;stations=1234,1356;what=temp;from=23423423000;sortBy=temp_min;to=3453454353000;interval=1;\n");
            new Query("stations=1234,1356;from=23423423000;to=3453454353000;interval=1;sortBy=temp_min;limit=10;filter=temp,>,-1;\n");
            new Query("sortBy=temp_max;stations=1234,1356;from=23423423000;to=3453454353000;interval=1;what=temp,wind;limit=10;filter=temp,<,10\n");
            new Query("stations=1234,1356; \t from=23423423000;to=3453454353000; \n interval=1;what=temp,wind;sortBy= wind_max; limit=10;filter=temp, <, 10\n");

            Logger.log("PARSE TIMESTAMP: 1 "  + assertQuery(new Query("stations=85670;                           ").getDataFiles(), DEBUG, FILE_COUNT));
            Logger.log("PARSE TIMESTAMP: 2 "  + assertQuery(new Query("stations=85670; from=0;              to=-1").getDataFiles(), DEBUG, FILE_COUNT));
            Logger.log("PARSE TIMESTAMP: 3a " + assertQuery(new Query("stations=85670; from=1549061102;     to=1549062102").getDataFiles(), DEBUG, 1000));
            Logger.log("PARSE TIMESTAMP: 3b " + assertQuery(new Query("stations=85670; from=1549061102000;  to=1549062102000").getDataFiles(), DEBUG, 1000));
            Logger.log("PARSE TIMESTAMP: 4a " + assertQuery(new Query("stations=85670; from=1549061000;     to=1549062000").getDataFiles(), DEBUG, 1000));
            Logger.log("PARSE TIMESTAMP: 4b " + assertQuery(new Query("stations=85670; from=1549061000000;  to=1549062000000").getDataFiles(), DEBUG, 1000));

            Logger.log("PARSE INTERVAL: 1 " + assertQuery(new Query("stations=85670;from=1549061000;to=1549062000;interval=1000;").getDataFiles(), DEBUG, 1));
            Logger.log("PARSE INTERVAL: 2 " + assertQuery(new Query("stations=85670;from=1549061000;to=1549062001;interval=1000;").getDataFiles(), DEBUG, 2));

            Logger.log("PARSE LIMIT: 1 " + assertQuery(new Query("stations=85670;from=1549061102000;to=1549062102000;limit=100").getDataFiles(), DEBUG, 100)); // 1000
            Logger.log("PARSE LIMIT: 2 " + assertQuery(new Query("stations=85670;from=1549061000;to=1549062001;interval=1000;limit=1").getDataFiles(), DEBUG, 1)); // 2

            Logger.log("PARSE SORTED: 1 " + assertQuery(new Query("stations=85670;sortBy=temp_min;").getDataFiles(), DEBUG, 1));
//
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
