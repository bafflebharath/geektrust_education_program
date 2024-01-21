package com.example.geektrust;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MainTest {

    private Main geekdemyApp;

    @BeforeEach
    void setUp() {
        geekdemyApp = new Main();
    }

    @Test
    void testCalculateCost() {
        assertEquals(3000, geekdemyApp.calculateCost(1, 1));
        assertEquals(10000, geekdemyApp.calculateCost(2, 2));
        assertEquals(7500, geekdemyApp.calculateCost(3, 3));
        assertEquals(0, geekdemyApp.calculateCost(4, 1));
    }

    @Test
    void testApplyB4G1Coupon() {
        int[] programCounts = {2, 1, 3};
        geekdemyApp.applyB4G1Coupon(programCounts);
        assertEquals(3, programCounts[0]);
    }

    @Test
    void testApplyOtherCoupons() {
        // Create a temporary file and write sample input
        try {
            Path tempDir = Files.createTempDirectory("geekdemy-test");
            Path tempFile = tempDir.resolve("test_input.txt");

            String input = "1\n2\nY\n2\n";
            Files.write(tempFile, input.getBytes());

            Scanner scanner = new Scanner(tempFile.toFile());

            double totalCost = 5000;
            int totalPrograms = 2;

            geekdemyApp.applyOtherCoupons(scanner, totalCost, totalPrograms);
            assertEquals(4750, totalCost);

        } catch (IOException e) {
            fail("IOException occurred while creating temporary file.");
        }
    }
}