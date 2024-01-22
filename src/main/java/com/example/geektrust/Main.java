package com.example.geektrust;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class Program {
    String category;
    int quantity;

    public Program(String category, int quantity) {
        this.category = category;
        this.quantity = quantity;
    }

    public double getCost() {
        switch (category) {
            case "CERTIFICATION":
                return quantity * 3000;
            case "DEGREE":
                return quantity * 5000;
            case "DIPLOMA":
                return quantity * 2500;
            default:
                return 0;
        }
    }
}

class Coupon {
    String type;

    public Coupon(String type) {
        this.type = type;
    }

    public double applyCoupon(double totalCost) {
        switch (type) {
            case "B4G1":
                return totalCost - getLowestProgramCost();
            case "DEAL_G20":
                return totalCost - (totalCost * 0.20);
            case "DEAL_G5":
                return totalCost - (totalCost * 0.05);
            default:
                return totalCost;
        }
    }

    private double getLowestProgramCost() {
        // Implement logic to find the lowest program cost
        return 0;
    }
}

class BillingSystem {
    double subtotal = 0;
    double couponDiscount = 0;
    double totalProDiscount = 0;
    double proMembershipFee = 0;
    double enrollmentFee = 0;
    double total = 0;

    public void addProgram(Program program) {
        subtotal += program.getCost();
    }

    public void applyCoupon(Coupon coupon) {
        subtotal = coupon.applyCoupon(subtotal);
        couponDiscount = subtotal * 0.05; // Assume a fixed 5% discount for all coupons
    }

    public void applyProMembership(String category) {
        switch (category) {
            case "CERTIFICATION":
                totalProDiscount += subtotal * 0.02;
                break;
            case "DEGREE":
                totalProDiscount += subtotal * 0.03;
                break;
            case "DIPLOMA":
                totalProDiscount += subtotal * 0.01;
                break;
        }
        proMembershipFee = 200;
    }

    public void applyEnrollmentFee() {
        if (subtotal < 6666) {
            enrollmentFee = 500;
            subtotal += enrollmentFee;
        }
    }

    public void printBill() {
        total = subtotal - couponDiscount - totalProDiscount + proMembershipFee;

        System.out.println("SUB_TOTAL\t" + String.format("%.2f", subtotal));
        System.out.println("COUPON_DISCOUNT\tB4G1\t" + String.format("%.2f", couponDiscount));
        System.out.println("TOTAL_PRO_DISCOUNT\t" + String.format("%.2f", totalProDiscount));
        System.out.println("PRO_MEMBERSHIP_FEE\t" + String.format("%.2f", proMembershipFee));
        System.out.println("ENROLLMENT_FEE\t" + String.format("%.2f", enrollmentFee));
        System.out.println("TOTAL\t" + String.format("%.2f", total));
    }
}

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java GeekdemyBillingSystem <input_file_path>");
            return;
        }

        BillingSystem billingSystem = new BillingSystem();

        try (BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                switch (tokens[0]) {
                    case "ADD_PROGRAMME":
                        Program program = new Program(tokens[1], Integer.parseInt(tokens[2]));
                        billingSystem.addProgram(program);
                        break;
                    case "PRO_MEMBERSHIP":
                        if (tokens[1].equals("Y")) {
                            billingSystem.applyProMembership("CERTIFICATION"); // Assuming pro membership is for certification programs
                        }
                        break;
                    case "APPLY_COUPON":
                        Coupon coupon = new Coupon(tokens[1]);
                        billingSystem.applyCoupon(coupon);
                        break;
                    case "PRINT_BILL":
                        billingSystem.applyEnrollmentFee();
                        billingSystem.printBill();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}