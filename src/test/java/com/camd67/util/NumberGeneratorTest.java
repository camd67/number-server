package com.camd67.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumberGeneratorTest {
    @Test
    void generatesNineDigitNumbers() {
        // This test is random so we'll run through a few iterations to check
        // bump this number up higher during dev to really test it out
        for (var i = 0; i < 10_000; i++) {
            var generated = NumberGenerator.generate();

            assertEquals(9, generated.length());
            for (var j = 0; j < 9; j++) {
                assertTrue(Character.isDigit(generated.charAt(j)), generated + " contained non-numerics");
            }
        }
    }
}