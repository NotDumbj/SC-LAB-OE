package Advance.BankSystem;

import java.util.ArrayList;

public abstract class BankAccount {
    protected final String accountNumber;
    protected double balance;
    protected final String customerId;
    protected final ArrayList<Transaction> transactions = new ArrayList<>();

    public BankAccount(String customerId) {
        this.accountNumber = UUIDGenerator.generate();
        this.customerId = customerId;
        this.balance = 0.0;
    }

    public abstract void deposit(double amount);
    public abstract void withdraw(double amount) throws Exception;

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public double getBalance() { return balance; }

    public void addTransaction(Transaction t) { transactions.add(t); }

    public void showTransactions() {
        for (Transaction t : transactions) System.out.println(t);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " | Acc#: " + accountNumber + " | Balance: " + balance;
    }
}
