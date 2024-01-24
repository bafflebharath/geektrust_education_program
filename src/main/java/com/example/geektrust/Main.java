package com.example.geektrust;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Context
class GeekdemyCart {
    private final List<Programme> programmes = new ArrayList<>();
    private DiscountStrategy discountStrategy;
    private EnrollmentStrategy enrollmentStrategy;
    private ProMembershipStrategy proMembershipStrategy;
    private double proMembershipFee;

    private double subtotal;

    private double discount;

    private double enrollmentFee;

    private double proMembershipDiscount;

    private String appliedCoupon;

    public String getAppliedCoupon() {
        return appliedCoupon;
    }

    public void setAppliedCoupon(String coupon) {
        this.appliedCoupon = coupon;
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    public void setEnrollmentStrategy(EnrollmentStrategy enrollmentStrategy) {
        this.enrollmentStrategy = enrollmentStrategy;
    }

    public void setProMembershipStrategy(ProMembershipStrategy proMembershipStrategy) {
        this.proMembershipStrategy = proMembershipStrategy;
    }

    public void addProMembershipFee() {
        this.proMembershipFee = 200;
    }

    public double getProMembershipFee() {
        return proMembershipFee;
    }

    public void addProgramme(Programme programme) {
        programmes.add(programme);
    }

    public List<Programme> getProgrammes() {
        return programmes;
    }

    public double calculateSubtotal() {
        subtotal = programmes.stream().mapToDouble(Programme::getCost).sum();
        return subtotal;
    }

    public double calculateProMembershipDiscount() {
        if(proMembershipFee>0){
            proMembershipDiscount = proMembershipStrategy.calculateDiscount(programmes);
        }
        return proMembershipDiscount;
    }

    private long totalDiscountableQuantity(List<Programme> programmes) {
        return programmes.stream()
                .filter(p -> isDiscountableProgramme(p.getType()))
                .mapToInt(Programme::getQuantity)
                .sum();
    }

    private boolean isDiscountableProgramme(String type) {
        return type.equals("CERTIFICATION") || type.equals("DEGREE") || type.equals("DIPLOMA");
    }

    public double calculateDiscount(List<Programme> programmes) {
        if (totalDiscountableQuantity(programmes) >= 4) {
            discount = programmes.stream()
                    .filter(p -> isDiscountableProgramme(p.getType()))
                    .min(Comparator.comparingDouble(Programme::getSingleCost))
                    .map(Programme::getSingleCost)
                    .orElse((double) 0);
            return discount;
        }else{
            double beforeDiscount = subtotal + enrollmentFee + proMembershipFee - proMembershipDiscount;
            discount = discountStrategy.calculateDiscount(beforeDiscount);
            return discount;
        }
    }

    public double calculateEnrollmentFee() {
        enrollmentFee = enrollmentStrategy.calculateEnrollmentFee(subtotal);
        return enrollmentFee;
    }

    public double calculateTotalFare() {
        return subtotal + enrollmentFee + proMembershipFee - proMembershipDiscount - discount;
    }
}

// Strategy Interfaces
interface DiscountStrategy {
    double calculateDiscount(double beforeDiscount);
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
    public double calculateDiscount(double beforeDiscount) {
        return 0;
    }
}

class DealG20Discount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double beforeDiscount) {
        return (beforeDiscount >= 0) ? beforeDiscount * 0.20 : 0;
    }
}

class DealG5Discount implements DiscountStrategy {
    @Override
    public double calculateDiscount(double beforeDiscount) {
        return (beforeDiscount >= 0) ? beforeDiscount * 0.05 : 0;
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
        return programmes.stream().mapToDouble(p -> p.getDiscountRate() * p.getCost()).sum();
    }
}

// Programme Class
class Programme {
    private final String type;
    private final int quantity;

    public Programme(String type, int quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSingleCost() {
        return switch (type) {
            case "CERTIFICATION" -> 3000;
            case "DEGREE" -> 5000;
            case "DIPLOMA" -> 2500;
            default -> 0;
        };
    }

    public double getCost() {
        return switch (type) {
            case "CERTIFICATION" -> 3000 * quantity;
            case "DEGREE" -> 5000 * quantity;
            case "DIPLOMA" -> 2500 * quantity;
            default -> 0;
        };
    }

    public double getDiscountRate() {
        return switch (type) {
            case "CERTIFICATION" -> 0.02;
            case "DEGREE" -> 0.03;
            case "DIPLOMA" -> 0.01;
            default -> 0;
        };
    }
}

public class Main {
    private static int count = 0;
    private static boolean discountFlag = false;
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <inputFilePath>");
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
                    count +=Integer.parseInt(tokens[2]);
                    cart.addProgramme(new Programme(tokens[1], Integer.parseInt(tokens[2])));
                }
                break;
            case "PRO_MEMBERSHIP":
                if (tokens.length == 2 && tokens[1].equalsIgnoreCase("Y")) {
                    applyProMembershipCoupon(cart);
                }
                break;
            case "APPLY_COUPON":
                if (count>=4 && tokens.length == 2) {
                    cart.setAppliedCoupon("B4G1");
                }else if(!discountFlag && count<4){
                    discountFlag = true;
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
            case "deal_g20":
                cart.setDiscountStrategy(new DealG20Discount());
                cart.setAppliedCoupon("Deal_G20");
                break;
            case "deal_g5":
                cart.setDiscountStrategy(new DealG5Discount());
                cart.setAppliedCoupon("DEAL_G5");
                break;
            default:
                break;
        }
    }

    private static void applyProMembershipCoupon(GeekdemyCart cart) {
            cart.calculateProMembershipDiscount();
            cart.setProMembershipStrategy(new ProMembershipDiscount());
            cart.addProMembershipFee(); // Add the Pro Membership fee
    }

    private static void printBill(GeekdemyCart cart) {
        System.out.printf("SUB_TOTAL  %.2f%n", cart.calculateSubtotal());
        System.out.printf("TOTAL_PRO_DISCOUNT   %.2f%n", cart.calculateProMembershipDiscount());
        System.out.printf("PRO_MEMBERSHIP_FEE   %.2f%n", cart.getProMembershipFee());
        System.out.printf("ENROLLMENT_FEE   %.2f%n", cart.calculateEnrollmentFee());
        System.out.printf("COUPON_DISCOUNT   %s    %.2f%n", cart.getAppliedCoupon(), cart.calculateDiscount(cart.getProgrammes()));
        System.out.printf("TOTAL   %.2f%n", cart.calculateTotalFare());
    }
}