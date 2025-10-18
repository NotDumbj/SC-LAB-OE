 BankSystem.java

 A human-friendly, full-featured ATM + Bank backend implementation for the open-ended lab.

 Features implemented:
 - Customer management with unique customer IDs, names, PINs and a collection of BankAccount objects
 - Abstract BankAccount with two subclasses: SavingsAccount and CheckingAccount
   - SavingsAccount enforces a minimum balance
   - CheckingAccount supports an overdraft limit
 - Transaction class with comprehensive details and per-account transaction history (ArrayList<Transaction>)
 - ATM interface with secure login (customer ID + PIN), blocks after 3 failed PIN attempts (temporary block)
 - Account selection, balance check, deposit, withdraw, transfer, view transaction history and text receipt printing
 - Bank backend with HashMap-based lookup for customers and accounts, initialization with sample data
 - Custom exceptions: InsufficientFundsException, InvalidAccountException, AccountBlockedException, InvalidAmountException
 - Logging (console + simple file logging) of important events

 DESIGN DOCUMENT

 UML-like class summary (textual)

 BankSystem (main)
  - Bank bank
  - ATM atm

 Bank
  - name: String
  - customers: HashMap<String, Customer>           // key: customerId
  - accounts: HashMap<String, BankAccount>        // key: accountNumber
  - methods: registerCustomer(...), createAccount(...), findAccount(...), findCustomer(...), ...

 Customer
  - customerId: String
  - name: String
  - pin: String
  - accounts: ArrayList<BankAccount>
  - failedPinAttempts: int
  - blockedUntil: LocalDateTime (nullable)

 BankAccount (abstract)
  - accountNumber: String
  - customerId: String
  - balance: double
  - status: AccountStatus (ACTIVE, BLOCKED, CLOSED)
  - transactions: ArrayList<Transaction>
  - abstract deposit(amount), withdraw(amount)

 SavingsAccount extends BankAccount
  - minimumBalance: double
  - periodic interest logic (optional)

 CheckingAccount extends BankAccount
  - overdraftLimit: double
  - can go negative down to -overdraftLimit

 Transaction
  - transactionId, type, amount, timestamp, fromAccount, toAccount, status, memo

 Enums:
 - TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER, INTEREST }
 - TransactionStatus { SUCCESS, FAILED_INSUFFICIENT_FUNDS, FAILED_INVALID_ACCOUNT, FAILED_INVALID_AMOUNT, FAILED_OTHER }
 - AccountStatus { ACTIVE, BLOCKED, CLOSED }

 Data Structure Justification
 - Bank.customers: HashMap<String, Customer>
     Fast O(1) access to customer by customer ID (needed for login and management).
 - Bank.accounts: HashMap<String, BankAccount>
     Fast lookup for account operations (deposit, withdraw, transfers).
 - Customer.accounts: ArrayList<BankAccount>
     Simple ordered collection for a customer's accounts. Items are relatively few per customer and we iterate for display.
 - BankAccount.transactions: ArrayList<Transaction>
     Transaction history needs to be kept as an append-only list, iterated in chronological order.

 Inheritance & Polymorphism
 - BankAccount defines the generic interface (deposit/withdraw/getBalance). ATM and Bank work with BankAccount references.
 - Concrete subclasses (SavingsAccount, CheckingAccount) override deposit/withdraw to enforce rules (min balance, overdraft).
 - Polymorphism allows ATM/Bank to operate generically (call withdraw/deposit on BankAccount without knowing concrete type).

 Error Handling Strategy
 - Domain-specific custom exceptions for clarity: InsufficientFundsException, InvalidAccountException, AccountBlockedException, InvalidAmountException.
 - Input validation with loops in the UI (readDouble/readPositiveDouble etc.) to avoid NumberFormatException crashing program flow.
 - Security: blocked access is implemented at the Customer level using a failedPinAttempts counter and blockedUntil timestamp.

 Block behavior (design decision)
 - After 3 consecutive incorrect PIN attempts, the customer is temporarily blocked for a configurable duration (DEFAULT_BLOCK_MINUTES=1).
 - This is documented and implementable: an admin or time expiration will restore access automatically after the block period.


Quick usage notes & testing tips

After you compile/run, choose 2 at the ATM main menu to view sample customers. Use a full customer ID (copy it exactly from the displayed list) and try the PINs:

Alice: PIN 1111

Bob: PIN 2222

Carol: PIN 3333

Try invalid PINs three times to see the temporary block behavior.

When transferring, destination account requires the full account UUID (use the 36-character ID printed in account lists or initialize additional UI to show full ids). For convenience, the UI displays short IDs for selection; transfers require full account ID to simulate real-world requirement.

Transaction logs are appended to banksystem.log and banksystem.log will show events. Per-account transaction histories are in memory (attached to each BankAccount).