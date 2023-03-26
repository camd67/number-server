package com.camd67.numberserver;

import com.google.common.hash.BloomFilter;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


/**
 * Aggregates numbers across all our listeners.
 * Note that this class must be thread safe as many other classes will
 * invoke and reach into it!
 * The main pattern for usage of this class is to pass an instance into anyone
 * who needs it, and submit events to it.
 */
public class NumberAggregator implements Closeable {
    /**
     * Immutable report on the current state of the number aggregation.
     */
    public static class Report {
        public final int duplicatesSeen;
        public final int uniqueTotal;

        public Report(int duplicatesSeen, int uniqueTotal) {
            this.duplicatesSeen = duplicatesSeen;
            this.uniqueTotal = uniqueTotal;
        }
    }

    private final BufferedWriter output;

    /**
     * The main de-dupe logic.
     * This set can contain up to 1 billion values!
     */
    private final Set<Integer> knownValues = new HashSet<>();

    private int duplicatesSeen;


    public NumberAggregator() throws IOException {
        var numbersLog = new File("numbers.log");
        // ignore result, we don't care if this is new or not since we'll set append=false on the writer
        numbersLog.createNewFile();
        output = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(numbersLog, false)
            )
        );
    }

    public Report generateReport() {
        return new Report(duplicatesSeen, knownValues.size());
    }

    /**
     * Accepts a number to be flushed to our numbers log.
     */
    public void acceptNumber(String number) {
        var num = Integer.parseInt(number);
        if (knownValues.add(num)) {
            logNumber(num);
        } else {
            duplicatesSeen++;
        }
    }

    /**
     * sync needed
     */
    private void logNumber(int number) {
        try {
            // This could be PrintWriter like our other writers but...
            // that thing has some annoying error handling.

            // Important!
            // Make sure that we do a single "atomic" output write here.
            // If we were to do something like output on two lines we may get race
            // conditions where the number and line separator are split.
            output.write(number + System.lineSeparator());
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
