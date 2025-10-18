package Advance.BankSystem;

public class CheckingAccount extends BankAccount {
    private static final double OVERDRAFT_LIMIT = -5000;

    public CheckingAccount(String customerId) {
        super(customerId);
    }

    @Override
    public void deposit(double amount) {
        balance += amount;
        addTransaction(new Transaction(TransactionType.DEPOSIT, amount, accountNumber, null, "SUCCESS"));
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (balance - amount < OVERDRAFT_LIMIT)
            throw new Exception("Overdraft limit reached!");
        balance -= amount;
        addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, "SUCCESS"));
    }
}
