package shared;

public class Logger {
    static int padding = 100;

    /*
        Helper function
    */
    public static void log(Object o) {
        if (Settings.SHOW_LOG) {
//            System.out.print(new java.util.Date() + " - ");

            System.out.printf("%-"+padding+"s", o);
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            System.out.println(stackTrace[2]);
        }
    }

    public static void error(Object o) {
        if (Settings.SHOW_DEBUG) {
//            System.err.print(new java.util.Date() + " - ");
            System.err.printf("%-"+padding+"s", o);
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            System.err.println(stackTrace[2]);
        }
    }

    public static void printStacktrace() {
        if (Settings.SHOW_DEBUG) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement ste : stackTrace)
                System.err.println(ste);
        }
    }
}
