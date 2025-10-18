package Advance.BankSystem;

import java.util.Scanner;

public class BankSystem {
    public static void main(String[] args) {
        Bank bank = new Bank();
        bank.initializeSampleData();

        ATM atm = new ATM(bank);
        Scanner sc = new Scanner(System.in);

        System.out.println("Starting JB Bank system...");
        System.out.println("Tip: sample customers exist. Use option '2' in the ATM to view them.");

        while (true) {
            System.out.println("\n=== Welcome to Friendly ATM ===");
            System.out.println("Main options:");
            System.out.println("1) Customer login (ATM)");
            System.out.println("2) Administrator login");
            System.out.println("3) View sample customer list");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            int choice = Integer.parseInt(sc.nextLine());
            switch (choice) {
                case 1 -> atm.start();
                case 2 -> new AdminInterface(bank).start();
                case 3 -> bank.displaySampleCustomers();
                case 0 -> {
                    System.out.println("Thank you for using Friendly ATM. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option, try again.");
            }
        }
    }
}
