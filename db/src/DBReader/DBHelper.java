package DBReader;

import shared.Settings;

public class DBHelper
{
    static String timestampToFolder(long timestamp) {
        return timestampToFolder( padLeft(""+timestamp, 8).replace(' ', '0' ) );
    }

    public static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    static String timestampToFolder(String path) {
        return Settings.DATA_PATH + "/" + path.replaceAll("(.{2})", "$1/").replace("/00", "/").replace("//", "/");
    }

    static int getNth2pair(long value, int nth) {
        return (int) ((value / (int) Math.pow(10, 8 - (nth * 2))) % 100);
    }
}
