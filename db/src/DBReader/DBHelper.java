package DBReader;

import shared.Logger;
import shared.Settings;

public class DBHelper
{
    private static int TIMESTAMP_LENGTH = 10;
    /**
     * @param s String to pad from the right
     * @param n mow much to pad
     * @return n=2 => "s  "
     */
    static String padRight(String s, int n)
    {
        return String.format("%-" + n + "s", s);
    }

    /**
     * @param s String to pad from the left
     * @param n mow much to pad
     * @return n=2 => "  s"
     */
    static String padLeft(String s, int n)
    {
        return String.format("%" + n + "s", s);
    }

    /**
     * @param timestamp timestamp as a number
     * @return /ab/cd/ef/gh/
     */
    static String timestampToFolder(long timestamp)
    {
        return timestampToFolder( padLeft(""+timestamp, TIMESTAMP_LENGTH).replace(' ', '0' ) );
    }

    /**
     * @param timestamp timestamp as a number
     * @return /ab/cd/ef/gh/
     */
    static String timestampToFolder(String timestamp)
    {
        String path = padRight(timestamp, TIMESTAMP_LENGTH).substring(0, TIMESTAMP_LENGTH).replace(" ", "0"); // we correct any timestamp to the length of 10

        if (path.length() != 10)
            Logger.error("WARNING: We correct any timestampPath to length 10 : " + path + " => " + timestamp);

        String pathWithSlashes  = slashify(path);
        return Settings.DATA_PATH + "/" + pathWithSlashes.substring(0, pathWithSlashes.length() -3); // -3 because the last 2 numbers form the file not the folder
    }

    /**
     * @param path timestamp
     * @return /ab/cd/ef/gh/
     */
    static String slashify(String path)
    {
        return path.replaceAll("(.{2})", "$1/");
    }

    /**
     * @param value timestamp
     * @param nth the folder depth
     * @return /ab/cd/ef/gh/ij/ 0=ab 1=cd 2=ef ...
     */
    static int getNth2pair(long value, int nth)
    {
        return (int) ((value / (int) Math.pow(10, 8 - (nth * 2))) % 100);
    }
}
