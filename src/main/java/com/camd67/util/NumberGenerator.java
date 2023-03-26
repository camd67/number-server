package com.camd67.util;

import java.util.Random;

public class NumberGenerator {
    private static final Random rand = new Random();

    /**
     * Generates a random 9-digit number with leading 0's
     */
    public static String generate() {
        // This should get us at most a 9-digit number
        var num = rand.nextInt(1_000_000_000);
        return String.format("%09d", num);
    }
}
