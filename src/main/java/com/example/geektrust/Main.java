package com.example.geektrust;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Context
class GeekdemyCart {
    private final List<Programme> programmes = new ArrayList<>();
    private DiscountStrategy discountStrategy;
    private EnrollmentStrategy enrollmentStrategy;
    private ProMembershipStrategy proMembershipStrategy;

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public void setEnrollmentStrategy(EnrollmentStrategy enrollmentStrategy) {
        this.enrollmentStrategy = enrollmentStrategy;
    }

    public void setProMembershipStrategy(ProMembershipStrategy proMembershipStrategy) {
        this.proMembershipStrategy = proMembershipStrategy;
    }

    public void addProgramme(Programme programme) {
        programmes.add(programme);
    }

    public double calculateTotalCost() {
        double subtotal = programmes.stream().mapToDouble(Programme::getCost).sum();
        double proMembershipDiscount = proMembershipStrategy.calculateDiscount(programmes);
        double discount = discountStrategy.calculateDiscount(programmes);
        double enrollmentFee = enrollmentStrategy.calculateEnrollmentFee(subtotal - discount);

        return subtotal - discount - proMembershipDiscount + enrollmentFee;
    }

    public double calculateProMembershipDiscount() {
        return proMembershipStrategy.calculateDiscount(programmes);
    }
}

// Strategy Interfaces
interface DiscountStrategy {
    double calculateDiscount(List<Programme> programmes);
}

interface EnrollmentStrategy {
    double calculateEnrollmentFee(double totalCost);
}

interface ProMembershipStrategy {
    double calculateDiscount(List<Programme> programmes);
}

// Concrete Strategies
class B4G1Discount implements DiscountStrategy {
    @Override
    public double calculateDiscount(List<Programme> programmes) {
        long count = programmes.stream().filter(p -> p.getType().equals("CERTIFICATION") || p.getType().equals("DEGREE") || p.getType().equals("DIPLOMA")).count();
        return count / 4 * programmes.stream().mapToDouble(Programme::getCost).min().orElse(0);
    }
}

class DealG20Discount implements DiscountStrategy {
    @Override
    public double calculateDiscount(List<Programme> programmes) {
        double totalCost = programmes.stream().mapToDouble(Programme::getCost).sum();
        return totalCost >= 10000 ? totalCost * 0.20 : 0;
    }
}

class DealG5Discount implements DiscountStrategy {
    @Override
    public double calculateDiscount(List<Programme> programmes) {
        return programmes.size() >= 2 ? programmes.stream().mapToDouble(Programme::getCost).sum() * 0.05 : 0;
    }
}

class DefaultEnrollmentStrategy implements EnrollmentStrategy {
    @Override
    public double calculateEnrollmentFee(double totalCost) {
        return totalCost < 6666 ? 500 : 0;
    }
}

class ProMembershipDiscount implements ProMembershipStrategy {
    @Override
    public double calculateDiscount(List<Programme> programmes) {
        System.out.println("ProMembershipDiscount calculateDiscount");
        return programmes.stream().mapToDouble(p -> p.getDiscountRate() * p.getCost()).sum();
    }
}

// Programme Class
class Programme {
    private String type;
    private int quantity;

    public Programme(String type, int quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public double getCost() {
        switch (type) {
            case "CERTIFICATION":
                return 3000 * quantity;
            case "DEGREE":
                return 5000 * quantity;
            case "DIPLOMA":
                return 2500 * quantity;
            default:
                return 0;
        }
    }

    public double getDiscountRate() {
        switch (type) {
            case "CERTIFICATION":
                return 0.02;
            case "DEGREE":
                return 0.03;
            case "DIPLOMA":
                return 0.01;
            default:
                return 0;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java GeekdemyApp <inputFilePath>");
            System.exit(1);
        }

        String inputFilePath = args[0];

        GeekdemyCart cart = new GeekdemyCart();
        cart.setDiscountStrategy(new B4G1Discount()); // Default discount strategy
        cart.setEnrollmentStrategy(new DefaultEnrollmentStrategy()); // Default enrollment strategy
        cart.setProMembershipStrategy(new ProMembershipDiscount()); // Default pro membership strategy

        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                processCommand(cart, line);
            }

            printBill(cart);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processCommand(GeekdemyCart cart, String command) {
        String[] tokens = command.split(" ");
        switch (tokens[0]) {
            case "ADD_PROGRAMME":
                if (tokens.length == 3) {
                    cart.addProgramme(new Programme(tokens[1], Integer.parseInt(tokens[2])));
                }
                break;
            case "PRO_MEMBERSHIP":
                if (tokens.length == 2) {
                    applyProMembershipCoupon(cart, tokens[1]);
                }
                break;
            case "APPLY_COUPON":
                if (tokens.length == 2) {
                    applyCoupon(cart, tokens[1]);
                }
                break;
            case "PRINT_BILL":
                // Additional processing if needed before printing the bill
                break;
            default:
                break;
        }
    }

    private static void applyCoupon(GeekdemyCart cart, String coupon) {
        switch (coupon.toLowerCase()) {
            case "b4g1":
                cart.setDiscountStrategy(new B4G1Discount());
                break;
            case "deal_g20":
                cart.setDiscountStrategy(new DealG20Discount());
                break;
            case "deal_g5":
                cart.setDiscountStrategy(new DealG5Discount());
                break;
            default:
                break;
        }
    }

    private static void applyProMembershipCoupon(GeekdemyCart cart, String coupon) {
        if (coupon.equalsIgnoreCase("Y")) {
            cart.setProMembershipStrategy(new ProMembershipDiscount());
        }
    }

    private static void printBill(GeekdemyCart cart) {
        double subtotal = cart.calculateTotalCost();

        System.out.printf("SUB_TOTAL  %.2f%n", subtotal);
        System.out.printf("COUPON_DISCOUNT   %s    %.2f%n", "B4G1", cart.calculateTotalCost() - subtotal);
        System.out.printf("TOTAL_PRO_DISCOUNT   %.2f%n", cart.calculateProMembershipDiscount());
        System.out.printf("PRO_MEMBERSHIP_FEE   %.2f%n", 0.00);
        System.out.printf("ENROLLMENT_FEE   %.2f%n", 0.00);
        System.out.printf("TOTAL   %.2f%n", cart.calculateTotalCost());
    }
}
