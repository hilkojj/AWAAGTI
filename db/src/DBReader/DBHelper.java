package DBReader;

import shared.Settings;

public class DBHelper
{
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
     * @return /ab/cd/ef/gh/ij/
     */
    static String timestampToFolder(long timestamp)
    {
        return timestampToFolder( padLeft(""+timestamp, 8).replace(' ', '0' ) );
    }

    /**
     * @param path timestamp
     * @return /ab/cd/ef/gh/ij/
     */
    static String timestampToFolder(String path)
    {
        return Settings.DATA_PATH + "/" + path.replaceAll("(.{2})", "$1/").replace("/00", "/").replace("//", "/");
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
