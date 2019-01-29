package shared;

public class Logger {

    /*
        Helper function
    */
    public static void log(Object o) {
        if (Settings.SHOW_DEBUG) {
            System.err.print(new java.util.Date());
            System.err.print(": \t");
            System.err.println(o);
        }
    }

    public static void error(Object o) {
        if (Settings.SHOW_DEBUG) {
            System.err.print(new java.util.Date());
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            System.err.print(stackTrace[2]);
            System.err.println(o);
        }
    }
}
