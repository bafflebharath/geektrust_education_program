package com.example.geektrust;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    private static final double CERTIFICATION_COST = 3000.0;
    private static final double DEGREE_COST = 5000.0;
    private static final double DIPLOMA_COST = 2500.0;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <file_path>");
            return;
        }

        String filePath = args[0];
        try (Scanner fileScanner = new Scanner(new File(filePath))) {
            processCommands(fileScanner);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        }
    }

    private static void processCommands(Scanner scanner) {
        int totalPrograms = 0;
        int[] programCounts = new int[3];
        Map<Integer, Double> categoryCosts = new HashMap<>();

        while (scanner.hasNext()) {
            String command = scanner.next();

            switch (command) {
                case "ADD_PROGRAMME":
                    processAddProgramCommand(scanner, totalPrograms, programCounts, categoryCosts);
                    break;
                case "APPLY_COUPON":
                    applyDealG20Coupon(categoryCosts);
                    break;
                case "PRINT_BILL":
                    printBill(categoryCosts, totalPrograms, programCounts);
                    break;
                default:
                    System.out.println("Invalid command: " + command);
                    break;
            }
        }
    }

    private static void processAddProgramCommand(Scanner scanner, int totalPrograms,
                                                 int[] programCounts, Map<Integer, Double> categoryCosts) {
        System.out.println("\nChoose a category of program to add to the cart:");
        System.out.println("1. Certification - Rs.3000");
        System.out.println("2. Degree - Rs. 5000");
        System.out.println("3. Diploma - Rs. 2500");
        System.out.println("0. Finish adding programs");

        int choice;
        try {
            choice = scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid value.");
            return;
        }

        if (choice == 0) {
            return;
        } else if (choice >= 1 && choice <= 3) {
            System.out.print("Enter the number of programs in this category: ");
            int count = scanner.nextInt();

            totalPrograms += count;
            programCounts[choice - 1] += count;
            updateCategoryCosts(choice, count, categoryCosts);
        } else {
            System.out.println("Invalid choice. Please choose a valid option.");
        }
    }

    private static void updateCategoryCosts(int choice, int count, Map<Integer, Double> categoryCosts) {
        categoryCosts.put(choice, categoryCosts.getOrDefault(choice, 0.0) + calculateCost(choice, count));
    }

    private static void applyDealG20Coupon(Map<Integer, Double> categoryCosts) {
        double totalCost = categoryCosts.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalCost >= 10000) {
            categoryCosts.replaceAll((k, v) -> v * 0.8);
            System.out.println("DEAL_G20 coupon applied!");
        } else {
            System.out.println("DEAL_G20 coupon not applied. Total cost is less than Rs.10,000.");
        }
    }

    private static void printBill(Map<Integer, Double> categoryCosts, int totalPrograms, int[] programCounts) {
        double subTotal = categoryCosts.values().stream().mapToDouble(Double::doubleValue).sum();
        double couponDiscount = 0;
        double totalProDiscount = 0;
        double proMembershipFee = 0;
        double enrollmentFee = 0;

        if (totalPrograms >= 4) {
            couponDiscount = calculateCost(getMinIndex(programCounts) + 1, 1);
            System.out.printf("COUPON_DISCOUNT\tB4G1\t%.2f%n", couponDiscount);
        }

        // Assuming Pro Membership includes one additional program
        totalProDiscount = applyProMembershipDiscount(categoryCosts);
        if (totalProDiscount > 0) {
            System.out.printf("TOTAL_PRO_DISCOUNT\t%.2f%n", totalProDiscount);
        }

        double totalCost = subTotal - couponDiscount - totalProDiscount + proMembershipFee + enrollmentFee;
        System.out.printf("SUB_TOTAL\t%.2f%n", subTotal);
        System.out.printf("TOTAL\t%.2f%n", totalCost);
    }

    private static double calculateCost(int programCategory, int count) {
        switch (programCategory) {
            case 1:
                return count * CERTIFICATION_COST;
            case 2:
                return count * DEGREE_COST;
            case 3:
                return count * DIPLOMA_COST;
            default:
                return 0;
        }
    }

    private static int getMinIndex(int[] array) {
        int minIndex = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > 0 && (minIndex == -1 || array[i] < array[minIndex])) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    private static long applyProMembershipDiscount(Map<Integer, Double> categoryCosts) {
        return categoryCosts.entrySet().stream()
                .mapToLong(entry -> {
                    int category = entry.getKey();
                    double cost = entry.getValue();
                    double discount = switch (category) {
                        case 1 -> 0.02;
                        case 2 -> 0.03;
                        case 3 -> 0.01;
                        default -> 0;
                    };
                    return (long) Math.ceil(cost * discount);
                })
                .sum();
    }
}