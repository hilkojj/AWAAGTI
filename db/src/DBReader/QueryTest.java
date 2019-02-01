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
public class QueryTest
{
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
        return (i == value);
    }

    public static void main(String[] args)  // THESE ASSERTS DEPEND ON WHAT DATA YOU USE
    {
        try {
            boolean DEBUG = false;

            new Query("limit=10;stations=1234,1356;what=temp;from=23423423;sortBy=32432432;to=3453454353;interval=1;\n");
            Logger.log("Syntax: 1");

            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;sortBy=32432432;limit=10;filter=temp,>,-1;\n");
            Logger.log("Syntax: 2");

            new Query("stations=1234,1356;from=23423423;to=3453454353;interval=1;what=temp,wind;sortBy=32432432;limit=10;filter=temp,<,10\n");
            Logger.log("Syntax: 3");


            Query q1 = new Query("stations=1234,1356;\n");
            Logger.log("PARSE: 1 " + assertQuery(q1.getDataFiles(), DEBUG, 5));

            Query q2 = new Query("stations=50,7950;from=0;to=-1\n");
            Logger.log("PARSE: 2 " + assertQuery(q2.getDataFiles(), DEBUG, 5));

            Query q3 = new Query("stations=50,7950;from=1548690682;to=1548690699\n");
            Logger.log("PARSE: 3 " + assertQuery(q3.getDataFiles(), DEBUG, 3));

            Query q4 = new Query("stations=50,7950;from=1548690682;to=1548690699;filter=temp,<,0\n");
            Logger.log("PARSE: 4 " + assertQuery(q4.getDataFiles(), DEBUG, 1));

            Query q5 = new Query("stations=50,7950;from=0;to=-1;interval=2;\n");
            Logger.log("PARSE: 5 " + assertQuery(q5.getDataFiles(), DEBUG, 4));


            Query qs1 = new Query("stations=50,7950;sortBy=temp_min;limit=1\n");
            Logger.log("PARSE SORTED: 1 " + assertQuery(qs1.getDataFiles(), DEBUG, 1));

            Query qs2 = new Query("stations=50,7950;from=1548690682;to=1548690699;sortBy=temp_min;limit=1\n");
            File file = qs2.getDataFiles().iterator().next();
            boolean works = DBFile.read(file, DBValue.TEMP).getDataPoints().size() == 8000;
            Logger.log("PARSE SORTED: 2 " + works);

            Query qs3 = new Query("stations=50,7950;from=0;to=1548692209;sortBy=temp_min;\n");
            File file3 = qs3.getDataFiles().iterator().next();
            int works3 = DBFile.read(file3).getDataPoints().size(); // == 8000;
            Logger.log("PARSE SORTED: 3 " + works3);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
