package Advance.BankSystem;

import java.util.ArrayList;

public class Customer {
    private final String customerId;
    private final String name;
    private final String pin;
    private boolean blocked = false;
    private final ArrayList<BankAccount> accounts = new ArrayList<>();

    public Customer(String name, String pin) {
        this.customerId = UUIDGenerator.generate();
        this.name = name;
        this.pin = pin;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPin() { return pin; }
    public ArrayList<BankAccount> getAccounts() { return accounts; }

    public void addAccount(BankAccount acc) { accounts.add(acc); }

    public boolean isBlocked() { return blocked; }
    public void block() { blocked = true; }
    public void unblock() { blocked = false; }

    @Override
    public String toString() {
        return customerId + " | " + name + " | Accounts: " + accounts.size();
    }
}
