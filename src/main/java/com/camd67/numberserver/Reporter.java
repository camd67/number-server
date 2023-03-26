package com.camd67.numberserver;

import java.util.function.Supplier;

/**
 * Reports out the current state of the application.
 * Data is output based on the supplied report generator.
 */
public class Reporter implements Runnable {
    private final Supplier<NumberAggregator.Report> generateReport;
    private NumberAggregator.Report lastReport;

    public Reporter(Supplier<NumberAggregator.Report> generateReport) {
        this.generateReport = generateReport;
        lastReport = new NumberAggregator.Report(0, 0);
    }

    @Override
    public void run() {
        var report = generateReport.get();

        var uniqueDiff = report.uniqueTotal - lastReport.uniqueTotal;
        var dupeDiff = report.duplicatesSeen - lastReport.duplicatesSeen;
        System.out.println(
            "Received " + String.format("%,d", uniqueDiff) + " unique numbers, "
                + String.format("%,d", dupeDiff) + " duplicates. " +
                "Unique total: " + String.format("%,d", report.uniqueTotal)
        );
        lastReport = report;
    }
}
