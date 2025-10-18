package Advance.BankSystem;

import java.util.Scanner;

public class ATM {
    private final Bank bank;
    private final Scanner sc = new Scanner(System.in);

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public void start() {
        System.out.print("Enter customer ID: ");
        String id = sc.nextLine();
        Customer c = bank.getCustomer(id);

        if (c == null) {
            System.out.println("No such customer found.");
            return;
        }

        if (c.isBlocked()) {
            System.out.println("Account is blocked. Contact admin.");
            return;
        }

        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Enter PIN: ");
            String pin = sc.nextLine();
            if (pin.equals(c.getPin())) {
                showMenu(c);
                return;
            } else {
                attempts++;
                System.out.println("Incorrect PIN (" + attempts + "/3)");
            }
        }

        c.block();
        System.out.println("Too many failed attempts. Your account is now blocked.");
    }

    private void showMenu(Customer c) {
        int choice;
        do {
            System.out.println("\nWelcome, " + c.getName());
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transfer (within or between customers)");
            System.out.println("5. View Transaction History");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> viewBalance(c);
                case 2 -> deposit(c);
                case 3 -> withdraw(c);
                case 4 -> transfer(c);
                case 5 -> history(c);
                case 0 -> System.out.println("Logging out...");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 0);
    }

    private BankAccount chooseAccount(Customer c) {
        System.out.println("Select an account:");
        for (int i = 0; i < c.getAccounts().size(); i++) {
            System.out.println((i + 1) + ") " + c.getAccounts().get(i));
        }
        int index = Integer.parseInt(sc.nextLine()) - 1;
        return c.getAccounts().get(index);
    }

    private void viewBalance(Customer c) {
        BankAccount acc = chooseAccount(c);
        System.out.println("Current balance: " + acc.getBalance());
    }

    private void deposit(Customer c) {
        BankAccount acc = chooseAccount(c);
        System.out.print("Enter amount to deposit: ");
        double amt = Double.parseDouble(sc.nextLine());
        acc.deposit(amt);
        System.out.println("Deposit successful. New balance: " + acc.getBalance());
    }

    private void withdraw(Customer c) {
        BankAccount acc = chooseAccount(c);
        System.out.print("Enter amount to withdraw: ");
        double amt = Double.parseDouble(sc.nextLine());
        try {
            acc.withdraw(amt);
            System.out.println("Withdraw successful. New balance: " + acc.getBalance());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transfer(Customer c) {
        System.out.println("1) Transfer between own accounts");
        System.out.println("2) Transfer to another customer's account");
        int option = Integer.parseInt(sc.nextLine());

        if (option == 1) {
            System.out.println("Select source account:");
            BankAccount from = chooseAccount(c);
            System.out.println("Select destination account:");
            BankAccount to = chooseAccount(c);
            System.out.print("Enter amount: ");
            double amt = Double.parseDouble(sc.nextLine());
            if (bank.transferWithinCustomer(c.getCustomerId(), from.getAccountNumber(), to.getAccountNumber(), amt))
                System.out.println("Transfer successful!");
            else
                System.out.println("Transfer failed.");
        } else if (option == 2) {
            System.out.println("Select source account:");
            BankAccount from = chooseAccount(c);
            System.out.print("Enter destination account number: ");
            String dest = sc.nextLine();
            System.out.print("Enter amount: ");
            double amt = Double.parseDouble(sc.nextLine());
            if (bank.transferBetweenCustomers(from.getAccountNumber(), dest, amt))
                System.out.println("Transfer successful!");
            else
                System.out.println("Transfer failed.");
        }
    }

    private void history(Customer c) {
        BankAccount acc = chooseAccount(c);
        acc.showTransactions();
    }
}
