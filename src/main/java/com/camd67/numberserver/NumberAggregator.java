package com.camd67.numberserver;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


/**
 * Aggregates numbers across all our listeners.
 * Note that this class must be thread safe as many other classes will
 * invoke and reach into it!
 * The main pattern for usage of this class is to pass an instance into anyone
 * who needs it, and submit events to it.
 */
public class NumberAggregator implements Closeable {
    private final File numbersLog = new File("numbers.log");
    private final BufferedWriter output;

    public NumberAggregator() throws IOException {
        // ignore result, we don't care if this is new or not since we'll set append=false on the writer
        numbersLog.createNewFile();
        output = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(numbersLog, false)
            )
        );
    }

    /**
     * Accepts a number to be flushed to our numbers log.
     */
    public void acceptNumber(String number) {
        logNumber(number);
    }

    private void logNumber(String number) {
        try {
            // This could be PrintWriter like our other writers but...
            // that thing has some annoying error handling.
            output.write(number);
            output.write(System.lineSeparator());
            output.flush();
        } catch (IOException e) {
            System.err.println("Error writing to numbers log - " + number);
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
