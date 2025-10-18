package Advance.BankSystem;

import java.util.Scanner;

public class AdminInterface {
    private final Bank bank;
    private final Scanner sc = new Scanner(System.in);
    private final String ADMIN_USER = "admin";
    private final String ADMIN_PASS = "password";

    public AdminInterface(Bank bank) {
        this.bank = bank;
    }

    public void start() {
        System.out.print("Enter admin username: ");
        String u = sc.nextLine();
        System.out.print("Enter password: ");
        String p = sc.nextLine();

        if (!u.equals(ADMIN_USER) || !p.equals(ADMIN_PASS)) {
            System.out.println("Access denied.");
            return;
        }

        int choice;
        do {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. View all customers");
            System.out.println("2. View all accounts");
            System.out.println("3. Create new account for customer");
            System.out.println("4. Unblock customer");
            System.out.println("0. Exit admin mode");
            System.out.print("Choose: ");
            choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> bank.viewAllCustomers();
                case 2 -> bank.viewAllAccounts();
                case 3 -> createAccount();
                case 4 -> unblock();
                case 0 -> System.out.println("Exiting admin mode...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private void createAccount() {
        System.out.print("Enter customer ID: ");
        String id = sc.nextLine();
        System.out.print("Enter account type (Savings/Checking): ");
        String type = sc.nextLine();
        bank.createAccount(id, type);
    }

    private void unblock() {
        System.out.print("Enter customer ID to unblock: ");
        String id = sc.nextLine();
        bank.unblockCustomer(id);
    }
}
