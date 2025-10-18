package Advance.BankSystem;

import java.util.*;

public class Bank {
    private final HashMap<String, Customer> customers = new HashMap<>();
    private final HashMap<String, BankAccount> accounts = new HashMap<>();

    // Register and create customers/accounts
    public void registerCustomer(Customer c) {
        customers.put(c.getCustomerId(), c);
        log("CUSTOMER_REGISTERED | " + c.getCustomerId() + " | " + c.getName());
    }

    public void createAccount(String customerId, String type) {
        Customer c = customers.get(customerId);
        if (c == null) {
            System.out.println("Customer not found.");
            return;
        }
        BankAccount acc;
        if (type.equalsIgnoreCase("Savings")) acc = new SavingsAccount(customerId);
        else acc = new CheckingAccount(customerId);

        c.addAccount(acc);
        accounts.put(acc.getAccountNumber(), acc);
        log("ACCOUNT_CREATED | " + acc.getAccountNumber() + " | " + type + " | Owner:" + c.getName());
    }

    public Customer getCustomer(String id) {
        return customers.get(id);
    }

    public BankAccount getAccount(String accountNo) {
        return accounts.get(accountNo);
    }

    public void printReceipt(String type, String fromAcc, String toAcc, double amount) {
        System.out.println("\n====== TRANSACTION RECEIPT ======");
        System.out.println("Type: " + type);
        System.out.println("From Account: " + fromAcc);
        System.out.println("To Account: " + toAcc);
        System.out.println("Amount: $" + amount);
        System.out.println("Date: " + java.time.LocalDateTime.now());
        System.out.println("=================================\n");
    }

    // Initialize sample data
    public void initializeSampleData() {
        Customer c1 = new Customer("Alice Johnson", "1234");
        Customer c2 = new Customer("Bob Khan", "1111");
        Customer c3 = new Customer("Carol Lee", "2222");

        registerCustomer(c1);
        registerCustomer(c2);
        registerCustomer(c3);

        createAccount(c1.getCustomerId(), "Savings");
        createAccount(c1.getCustomerId(), "Checking");
        createAccount(c2.getCustomerId(), "Savings");
        createAccount(c3.getCustomerId(), "Checking");

        log("SAMPLE DATA INITIALIZED");
    }

    public void displaySampleCustomers() {
        System.out.println("Available sample customers (ID - name):");
        for (Customer c : customers.values()) {
            System.out.println("  " + c.getCustomerId() + " - " + c.getName());
        }
    }

    // =================== Transfer Logic ====================

    public boolean transferWithinCustomer(String customerId, String fromAcc, String toAcc, double amount) {
        BankAccount src = accounts.get(fromAcc);
        BankAccount dest = accounts.get(toAcc);
        if (src == null || dest == null) return false;
        if (!src.getCustomerId().equals(customerId) || !dest.getCustomerId().equals(customerId)) return false;
        try {
            src.withdraw(amount);
            dest.deposit(amount);
            printReceipt("INTRA-CUSTOMER TRANSFER", fromAcc, toAcc, amount);
            logTransfer(src, dest, amount, true);
            return true;
        } catch (Exception e) {
            logTransfer(src, dest, amount, false);
            return false;
        }
    }

    public boolean transferBetweenCustomers(String fromAcc, String toAcc, double amount) {
        BankAccount src = accounts.get(fromAcc);
        BankAccount dest = accounts.get(toAcc);
        if (src == null || dest == null) {
            System.out.println("Invalid account number(s).");
            return false;
        }
        try {
            src.withdraw(amount);
            dest.deposit(amount);
            printReceipt("CROSS-CUSTOMER TRANSFER", fromAcc, toAcc, amount);
            logTransfer(src, dest, amount, true);
            return true;
        } catch (Exception e) {
            logTransfer(src, dest, amount, false);
            return false;
        }
    }

    private void logTransfer(BankAccount from, BankAccount to, double amt, boolean success) {
        Transaction t = new Transaction(TransactionType.TRANSFER, amt, from.getAccountNumber(),
                to.getAccountNumber(), success ? "SUCCESS" : "FAILED");
        from.addTransaction(t);
        to.addTransaction(t);
    }

    // =================== Admin Functions ====================

    public void viewAllCustomers() {
        for (Customer c : customers.values()) {
            System.out.println(c);
        }
    }

    public void viewAllAccounts() {
        for (BankAccount acc : accounts.values()) {
            System.out.println(acc);
        }
    }

    public void unblockCustomer(String id) {
        Customer c = customers.get(id);
        if (c != null) {
            c.unblock();
            System.out.println("Customer " + c.getName() + " is now unblocked.");
        } else {
            System.out.println("Customer not found.");
        }
    }

    // Log helper
    private void log(String message) {
        System.out.println("(LOG) [" + java.time.LocalDateTime.now() + "] " + message);
    }
}
