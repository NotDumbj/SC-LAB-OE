import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/* -------------------- Enums -------------------- */
enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER, INTEREST }
enum TransactionStatus { SUCCESS, FAILED_INSUFFICIENT_FUNDS, FAILED_INVALID_ACCOUNT, FAILED_INVALID_AMOUNT, FAILED_OTHER }
enum AccountStatus { ACTIVE, BLOCKED, CLOSED }

/* -------------------- Exceptions -------------------- */
class InsufficientFundsException extends Exception { InsufficientFundsException(String msg) { super(msg); } }
class InvalidAccountException extends Exception { InvalidAccountException(String msg) { super(msg); } }
class AccountBlockedException extends Exception { AccountBlockedException(String msg) { super(msg); } }
class InvalidAmountException extends Exception { InvalidAmountException(String msg) { super(msg); } }

/* -------------------- Transaction -------------------- */
class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String fromAccount; // nullable
    private final String toAccount;   // nullable
    private final TransactionStatus status;
    private final String memo;

    public Transaction(TransactionType type, double amount, String fromAccount, String toAccount, TransactionStatus status, String memo) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.status = status;
        this.memo = memo;
    }

    public String toLogString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s | %s | %s | %.2f | FROM:%s | TO:%s | %s | %s",
                timestamp.format(f),
                transactionId,
                type,
                amount,
                fromAccount == null ? "-" : fromAccount,
                toAccount == null ? "-" : toAccount,
                status,
                memo == null ? "" : memo);
    }

    @Override
    public String toString() {
        return toLogString();
    }
}

/* -------------------- BankAccount (abstract) -------------------- */
abstract class BankAccount {
    protected final String accountNumber;
    protected final String customerId;
    protected double balance;
    protected AccountStatus status;
    protected final List<Transaction> transactions = new ArrayList<>();

    public BankAccount(String customerId, double initialBalance) {
        this.accountNumber = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.balance = Math.max(0, initialBalance);
        this.status = AccountStatus.ACTIVE;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public double getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }

    protected void addTransaction(Transaction t) { transactions.add(t); }

    // Returns TransactionStatus for caller convenience (and logs transaction inside)
    public abstract TransactionStatus withdraw(double amount) throws InvalidAmountException;
    public abstract TransactionStatus deposit(double amount) throws InvalidAmountException;

    public void blockAccount() { status = AccountStatus.BLOCKED; }
    public void activateAccount() { status = AccountStatus.ACTIVE; }

    public String toShortString() {
        return String.format("%s | %s | Balance: %.2f | %s", accountNumber, getAccountType(), balance, status);
    }

    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("%s | %s | Owner: %s | Balance: %.2f | %s",
                getAccountType(), accountNumber, customerId, balance, status);
    }
}

/* -------------------- SavingsAccount -------------------- */
class SavingsAccount extends BankAccount {
    private final double minimumBalance;
    private final double interestRate; // optional, annual (simple periodic application in example)

    public SavingsAccount(String customerId, double initialBalance, double minimumBalance, double interestRate) {
        super(customerId, initialBalance);
        this.minimumBalance = Math.max(0, minimumBalance);
        this.interestRate = Math.max(0, interestRate);
    }

    @Override
    public TransactionStatus withdraw(double amount) throws InvalidAmountException {
        if (status != AccountStatus.ACTIVE) {
            Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_OTHER, "Account not active");
            addTransaction(t);
            return TransactionStatus.FAILED_OTHER;
        }
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be > 0");
        double potential = balance - amount;
        if (potential < minimumBalance) {
            Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_INSUFFICIENT_FUNDS, "Would breach minimum balance");
            addTransaction(t);
            return TransactionStatus.FAILED_INSUFFICIENT_FUNDS;
        }
        balance -= amount;
        Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.SUCCESS, "Withdrawal successful");
        addTransaction(t);
        return TransactionStatus.SUCCESS;
    }

    @Override
    public TransactionStatus deposit(double amount) throws InvalidAmountException {
        if (status != AccountStatus.ACTIVE) {
            Transaction t = new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.FAILED_OTHER, "Account not active");
            addTransaction(t);
            return TransactionStatus.FAILED_OTHER;
        }
        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be > 0");
        balance += amount;
        Transaction t = new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.SUCCESS, "Deposit successful");
        addTransaction(t);
        return TransactionStatus.SUCCESS;
    }

    public void applyInterestPeriodically() {
        if (interestRate <= 0) return;
        double interest = balance * interestRate; // simplistic application
        if (interest > 0) {
            balance += interest;
            Transaction t = new Transaction(TransactionType.INTEREST, interest, null, accountNumber, TransactionStatus.SUCCESS, "Interest credited");
            addTransaction(t);
        }
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }
}

/* -------------------- CheckingAccount -------------------- */
class CheckingAccount extends BankAccount {
    private final double overdraftLimit; // positive number: how far negative balance can go

    public CheckingAccount(String customerId, double initialBalance, double overdraftLimit) {
        super(customerId, initialBalance);
        this.overdraftLimit = Math.max(0, overdraftLimit);
    }

    @Override
    public TransactionStatus withdraw(double amount) throws InvalidAmountException {
        if (status != AccountStatus.ACTIVE) {
            Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_OTHER, "Account not active");
            addTransaction(t);
            return TransactionStatus.FAILED_OTHER;
        }
        if (amount <= 0) throw new InvalidAmountException("Withdrawal amount must be > 0");
        double potential = balance - amount;
        if (potential < -overdraftLimit) {
            Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.FAILED_INSUFFICIENT_FUNDS, "Would exceed overdraft limit");
            addTransaction(t);
            return TransactionStatus.FAILED_INSUFFICIENT_FUNDS;
        }
        balance -= amount;
        Transaction t = new Transaction(TransactionType.WITHDRAWAL, amount, accountNumber, null, TransactionStatus.SUCCESS, "Withdrawal successful");
        addTransaction(t);
        return TransactionStatus.SUCCESS;
    }

    @Override
    public TransactionStatus deposit(double amount) throws InvalidAmountException {
        if (status != AccountStatus.ACTIVE) {
            Transaction t = new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.FAILED_OTHER, "Account not active");
            addTransaction(t);
            return TransactionStatus.FAILED_OTHER;
        }
        if (amount <= 0) throw new InvalidAmountException("Deposit amount must be > 0");
        balance += amount;
        Transaction t = new Transaction(TransactionType.DEPOSIT, amount, null, accountNumber, TransactionStatus.SUCCESS, "Deposit successful");
        addTransaction(t);
        return TransactionStatus.SUCCESS;
    }

    @Override
    public String getAccountType() { return "Checking"; }
}

/* -------------------- Customer -------------------- */
class Customer {
    private final String customerId;
    private final String name;
    private final String pin; // stored as plain string here for simplicity (note: not secure for real apps)
    private final List<BankAccount> accounts = new ArrayList<>();

    // security fields
    private int failedPinAttempts = 0;
    private LocalDateTime blockedUntil = null;

    public Customer(String name, String pin) {
        this.customerId = UUID.randomUUID().toString();
        this.name = name;
        this.pin = pin;
    }

    public String getCustomerId() { return customerId; }
    public String getName() { return name; }

    public boolean verifyPin(String inputPin) {
        // check if currently blocked
        if (isBlocked()) return false;
        boolean ok = this.pin.equals(inputPin);
        if (ok) {
            failedPinAttempts = 0;
        } else {
            failedPinAttempts++;
            if (failedPinAttempts >= ATM.DEFAULT_MAX_PIN_ATTEMPTS) {
                // block temporarily
                blockedUntil = LocalDateTime.now().plusMinutes(ATM.DEFAULT_BLOCK_MINUTES);
            }
        }
        return ok;
    }

    public boolean isBlocked() {
        if (blockedUntil == null) return false;
        if (LocalDateTime.now().isAfter(blockedUntil)) {
            // unblock automatically when time passes
            blockedUntil = null;
            failedPinAttempts = 0;
            return false;
        }
        return true;
    }

    public void addAccount(BankAccount a) { accounts.add(a); }
    public List<BankAccount> getAccounts() { return Collections.unmodifiableList(accounts); }
    public String toStringShort() { return String.format("%s | %s", name, customerId); }

    public LocalDateTime getBlockedUntil() { return blockedUntil; }
}

/* -------------------- Bank -------------------- */
class Bank {
    private final String name;
    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, BankAccount> accounts = new HashMap<>();
    private final Logger logger = new Logger("banksystem.log");

    public Bank(String name) {
        this.name = name;
    }

    public void registerCustomer(Customer c) {
        customers.put(c.getCustomerId(), c);
        logger.log(String.format("CUSTOMER_REGISTERED | %s | %s", c.getCustomerId(), c.getName()));
    }

    public Customer findCustomerById(String id) throws InvalidAccountException {
        Customer c = customers.get(id);
        if (c == null) throw new InvalidAccountException("Customer not found: " + id);
        return c;
    }

    public BankAccount findAccountByNumber(String accNum) throws InvalidAccountException {
        BankAccount a = accounts.get(accNum);
        if (a == null) throw new InvalidAccountException("Account not found: " + accNum);
        return a;
    }

    public void addAccountForCustomer(String customerId, BankAccount account) throws InvalidAccountException {
        Customer c = customers.get(customerId);
        if (c == null) throw new InvalidAccountException("Cannot add account: customer not found");
        c.addAccount(account);
        accounts.put(account.getAccountNumber(), account);
        logger.log(String.format("ACCOUNT_CREATED | %s | %s | Owner:%s", account.getAccountNumber(), account.getAccountType(), c.getName()));
    }

    public Collection<Customer> listAllCustomers() { return customers.values(); }
    public Collection<BankAccount> listAllAccounts() { return accounts.values(); }

    public void log(String text) { logger.log(text); }

    // helper to initialize sample data for demonstration & grading
    public void initializeSampleData() {
        // Create customers with simple PINs (for demo)
        Customer alice = new Customer("Alice Johnson", "1111");
        Customer bob = new Customer("Bob Khan", "2222");
        Customer carol = new Customer("Carol Lee", "3333");
        registerCustomer(alice);
        registerCustomer(bob);
        registerCustomer(carol);

        // Create accounts
        try {
            BankAccount a1 = new SavingsAccount(alice.getCustomerId(), 1000.0, 100.0, 0.01);
            addAccountForCustomer(alice.getCustomerId(), a1);

            BankAccount a2 = new CheckingAccount(alice.getCustomerId(), 200.0, 300.0); // overdraft 300
            addAccountForCustomer(alice.getCustomerId(), a2);

            BankAccount b1 = new SavingsAccount(bob.getCustomerId(), 500.0, 50.0, 0.005);
            addAccountForCustomer(bob.getCustomerId(), b1);

            BankAccount c1 = new CheckingAccount(carol.getCustomerId(), 50.0, 100.0);
            addAccountForCustomer(carol.getCustomerId(), c1);

            logger.log("SAMPLE DATA INITIALIZED");
        } catch (InvalidAccountException e) {
            logger.log("Error initializing sample data: " + e.getMessage());
        }
    }
}

/* -------------------- Simple Logger -------------------- */
class Logger {
    private final String filename;
    public Logger(String filename) { this.filename = filename; }

    public synchronized void log(String text) {
        String line = String.format("[%s] %s", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), text);
        System.out.println("(LOG) " + line);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Logger write failed: " + e.getMessage());
        }
    }
}

/* -------------------- ATM UI -------------------- */
class ATM {
    public static final int DEFAULT_MAX_PIN_ATTEMPTS = 3;
    public static final int DEFAULT_BLOCK_MINUTES = 1;

    private final Bank bank;
    private final Scanner sc = new Scanner(System.in);

    public ATM(Bank bank) { this.bank = bank; }

    public void start() {
        System.out.println("=== Welcome to Friendly ATM ===");
        while (true) {
            System.out.println("\nMain options:");
            System.out.println("1) Customer login (use customer ID + PIN)");
            System.out.println("2) View sample customer list (for demo)");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1": handleLogin(); break;
                case "2": showSampleCustomers(); break;
                case "0": System.out.println("Goodbye — thank you for using Friendly ATM."); return;
                default: System.out.println("Try again please (choose 1, 2 or 0).");
            }
        }
    }

    private void showSampleCustomers() {
        System.out.println("Available sample customers (ID - name) — use these IDs to log in for demo:");
        for (Customer c : bank.listAllCustomers()) {
            System.out.println("  " + c.getCustomerId() + " - " + c.getName());
        }
        System.out.println("Note: full IDs are required for login; these are shortened for display.");
    }

    private void handleLogin() {
        System.out.print("Enter full customer ID: ");
        String custId = sc.nextLine().trim();
        Customer c;
        try {
            c = bank.findCustomerById(custId);
        } catch (InvalidAccountException e) {
            System.out.println("No such customer found. (Tip: use option 2 to view sample customers.)");
            return;
        }

        if (c.isBlocked()) {
            System.out.println("Account temporarily blocked until: " + c.getBlockedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return;
        }

        System.out.print("Enter PIN: ");
        String pin = sc.nextLine().trim();

        boolean ok = c.verifyPin(pin);
        if (!ok) {
            if (c.isBlocked()) {
                System.out.println("Too many incorrect attempts. Your access is temporarily blocked until: " + c.getBlockedUntil().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                bank.log("CUSTOMER_BLOCKED | " + c.getCustomerId());
            } else {
                System.out.println("Incorrect PIN. Please try again.");
            }
            return;
        }

        System.out.println("Login successful. Welcome, " + c.getName() + "!");
        customerMenu(c);
    }

    private void customerMenu(Customer c) {
        while (true) {
            System.out.println("\nCustomer Menu — choose an operation:");
            System.out.println("1) Select account and Check Balance");
            System.out.println("2) Select account and Deposit");
            System.out.println("3) Select account and Withdraw");
            System.out.println("4) Transfer between accounts");
            System.out.println("5) View transaction history for an account");
            System.out.println("0) Logout");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": accountSelectAndShowBalance(c); break;
                    case "2": accountSelectAndDeposit(c); break;
                    case "3": accountSelectAndWithdraw(c); break;
                    case "4": transferBetweenAccounts(c); break;
                    case "5": showTransactionHistory(c); break;
                    case "0": System.out.println("Logged out. See you soon!"); return;
                    default: System.out.println("Please choose a valid option.");
                }
            } catch (Exception e) {
                System.out.println("Operation error: " + e.getMessage());
            }
        }
    }

    private BankAccount chooseAccountFromCustomer(Customer c) {
        List<BankAccount> accounts = c.getAccounts();
        if (accounts.isEmpty()) {
            System.out.println("You have no accounts.");
            return null;
        }
        System.out.println("Your accounts:");
        for (int i = 0; i < accounts.size(); i++) {
            BankAccount a = accounts.get(i);
            System.out.printf("  %d) %s | %s | Balance: %.2f | %s%n", i+1, a.getAccountNumber(), a.getAccountType(), a.getBalance(), a.getStatus());
        }
        System.out.print("Choose account number (1-" + accounts.size() + "): ");
        try {
            int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (idx < 0 || idx >= accounts.size()) {
                System.out.println("Invalid selection.");
                return null;
            }
            return accounts.get(idx);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return null;
        }
    }

    private void accountSelectAndShowBalance(Customer c) {
        BankAccount a = chooseAccountFromCustomer(c);
        if (a == null) return;
        System.out.printf("Account %s balance: %.2f%n", a.getAccountNumber(), a.getBalance());
    }

    private void accountSelectAndDeposit(Customer c) {
        BankAccount a = chooseAccountFromCustomer(c);
        if (a == null) return;
        double amt = readPositiveDouble("Enter amount to deposit: ");
        try {
            TransactionStatus status = a.deposit(amt);
            printReceipt(a, TransactionType.DEPOSIT, amt, status);
            bank.log("DEPOSIT | " + amt + " | ACC:" + a.getAccountNumber());
        } catch (InvalidAmountException e) {
            System.out.println("Invalid amount: " + e.getMessage());
        }
    }

    private void accountSelectAndWithdraw(Customer c) {
        BankAccount a = chooseAccountFromCustomer(c);
        if (a == null) return;
        double amt = readPositiveDouble("Enter amount to withdraw: ");
        try {
            TransactionStatus status = a.withdraw(amt);
            printReceipt(a, TransactionType.WITHDRAWAL, amt, status);
            bank.log("WITHDRAW | " + amt + " | ACC:" + a.getAccountNumber());
        } catch (InvalidAmountException e) {
            System.out.println("Invalid amount: " + e.getMessage());
        }
    }

    private void transferBetweenAccounts(Customer c) {
        System.out.println("Choose source account:");
        BankAccount from = chooseAccountFromCustomer(c);
        if (from == null) return;

        System.out.print("Enter destination account FULL number (not just short id): ");
        String dest = sc.nextLine().trim();
        BankAccount to;
        try {
            to = bank.findAccountByNumber(dest);
        } catch (InvalidAccountException e) {
            System.out.println("Destination account not found.");
            return;
        }

        double amt = readPositiveDouble("Enter amount to transfer: ");
        try {
            TransactionStatus withdrawStatus = from.withdraw(amt);
            if (withdrawStatus != TransactionStatus.SUCCESS) {
                System.out.println("Transfer failed at withdraw stage: " + withdrawStatus);
                printReceipt(from, TransactionType.TRANSFER, amt, withdrawStatus);
                return;
            }
            TransactionStatus depositStatus = to.deposit(amt);
            // record transfer transactions
            Transaction tFrom = new Transaction(TransactionType.TRANSFER, amt, from.getAccountNumber(), to.getAccountNumber(), TransactionStatus.SUCCESS, "Transfer out");
            Transaction tTo = new Transaction(TransactionType.TRANSFER, amt, from.getAccountNumber(), to.getAccountNumber(), TransactionStatus.SUCCESS, "Transfer in");
            from.addTransaction(tFrom);
            to.addTransaction(tTo);

            System.out.println("Transfer completed.");
            printReceipt(from, TransactionType.TRANSFER, amt, TransactionStatus.SUCCESS);
            bank.log("TRANSFER | " + amt + " | FROM:" + from.getAccountNumber() + " TO:" + to.getAccountNumber());
        } catch (InvalidAmountException e) {
            System.out.println("Invalid amount: " + e.getMessage());
        }
    }

    private void showTransactionHistory(Customer c) {
        BankAccount a = chooseAccountFromCustomer(c);
        if (a == null) return;
        List<Transaction> txs = a.getTransactions();
        if (txs.isEmpty()) {
            System.out.println("No transactions yet for this account.");
            return;
        }
        System.out.println("Transaction history (most recent last):");
        for (Transaction t : txs) {
            System.out.println("  " + t.toLogString());
        }
    }

    private void printReceipt(BankAccount account, TransactionType type, double amount, TransactionStatus status) {
        System.out.println("\n====== RECEIPT ======");
        System.out.println("Transaction: " + type);
        System.out.println("Account: " + account.getAccountNumber());
        System.out.printf("Amount: %.2f%n", amount);
        System.out.println("Status: " + status);
        System.out.printf("New Balance: %.2f%n", account.getBalance());
        System.out.println("Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("=====================\n");
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                double v = Double.parseDouble(line);
                if (v <= 0) {
                    System.out.println("Please enter a positive number.");
                } else {
                    return v;
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number (e.g., 1200.50).");
            }
        }
    }
}

/* -------------------- Main program -------------------- */
public class BankSystem {
    public static void main(String[] args) {
        Bank bank = new Bank("JB Bank");
        bank.initializeSampleData();
        ATM atm = new ATM(bank);

        System.out.println("Starting JB Bank system...");
        System.out.println("Tip: sample customers exist. Use option '2' in the ATM to view them.");
        atm.start();

        System.out.println("Shutting down JB2 Bank. Have a good day!");
    }
}
