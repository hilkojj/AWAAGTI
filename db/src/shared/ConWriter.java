package shared;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class ConWriter {

    private BufferedWriter conWriter = null;

    private String ERROR = "error";


    public ConWriter(OutputStreamWriter outStream) {
        conWriter = new BufferedWriter(outStream);
    }

    public void write(Types type, String str) {
        try {
            conWriter.write(type.toString() +"="+ str);
            conWriter.newLine();
            conWriter.flush();
        } catch (IOException e) {
            Logger.log("The client does not respond anymore");
        }
    }

    public void close() {
        try {
            conWriter.flush();
            conWriter.close();
        } catch (IOException e) { /* we close it anyway so error are of no use anymore */ }
    }

    public enum Types {
        file,       // The file url
        error,      // We can not run the query
        warning,    // We can run the query, but it has probably no meaning
        progress,   // [1-100] percentage of how far we are
    }
}

