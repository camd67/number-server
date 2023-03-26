package com.camd67.numberserver;

public class Reporter implements Runnable {
    private final long startTime;
    private long lastTime;

    public Reporter() {
        startTime = System.currentTimeMillis();
        lastTime = startTime;
    }

    @Override
    public void run() {
        var currentTime = System.currentTimeMillis();
        System.out.println("[REPORT] Running for " + ((currentTime - startTime) / 1000.0) + "s");
        System.out.println("[REPORT] Last message sent " + ((currentTime - lastTime) / 1000.0) + "s ago");
        lastTime = System.currentTimeMillis();
    }
}
