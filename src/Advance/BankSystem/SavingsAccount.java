package Advance.BankSystem;

public class SavingsAccount extends BankAccount {
    private static final double MIN_BALANCE = 1000;

    public SavingsAccount(String customerId) {
        super(customerId);
    }

    @Override
    public void deposit(double amount) {
        balance += amount;
        addTransaction(new Transaction(TransactionType.DEPOSIT, amount, accountNumber, null, "SUCCESS"));
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (balance - amount < MIN_BALANCE) throw new Exception("Minimum balance required!");
        balance -= amount;
        addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, "SUCCESS"));
    }
}
