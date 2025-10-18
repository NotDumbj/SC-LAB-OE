package Advance.BankSystem;

import java.time.LocalDateTime;

public class Transaction {
    private final String id;
    private final TransactionType type;
    private final double amount;
    private final String sourceAcc;
    private final String destAcc;
    private final LocalDateTime time;
    private final String status;

    public Transaction(TransactionType type, double amount, String src, String dest, String status) {
        this.id = UUIDGenerator.generate();
        this.type = type;
        this.amount = amount;
        this.sourceAcc = src;
        this.destAcc = dest;
        this.time = LocalDateTime.now();
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + time + "] " + type + " | From: " + sourceAcc + " | To: " + destAcc + " | Amount: " + amount + " | " + status;
    }
}
