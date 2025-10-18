Task 2: Advanced Feature — Funds Transfer & Bank Administration

(Continuation of the Comprehensive ATM & Bank System Project)

1. Overview

This document explains the new features and architectural updates introduced in Task 2, which extends the original banking system from Task 1.

The two major enhancements are:

Funds Transfer – enabling both intra-customer and cross-customer money transfers.

Bank Administrator Interface – a separate, secure administrative module to manage and monitor the bank’s data.

2. New Features Implementation
   2.1 Intra-Customer Funds Transfer (Within the Same Customer)

Purpose:
Allow a logged-in customer to transfer funds between their own accounts — for example, from a SavingsAccount to a CheckingAccount, or vice versa.

Implementation Logic:

The ATM presents all accounts owned by the logged-in customer.

The user selects:

Source account (account to withdraw from)

Destination account (account to deposit into)

The system performs validation checks:

Both accounts must belong to the same customer.

The source account must have sufficient funds (respecting its rules, such as minimum balance or overdraft limit).

The amount is withdrawn from the source account and deposited into the destination account.

Two Transaction objects are recorded:

One for the debit (withdrawal)

One for the credit (deposit)

A success receipt is displayed, confirming the transaction ID, amount, and updated balances.

Code Interaction:

Method: ATM.handleIntraCustomerTransfer(Customer customer)

Uses Bank.getAccountsByCustomerID() to retrieve eligible accounts.

Relies on polymorphism: each account type validates the withdrawal rules through overridden withdraw() implementations.

2.2 Cross-Customer Funds Transfer (Between Different Customers)

Purpose:
Enable one customer to send funds to another customer's account within the same simulated bank.

Implementation Logic:

The user enters the destination account number and transfer amount.

The system validates:

The destination account exists and is active.

The source account has sufficient funds (considering account type constraints).

The system performs:

A withdraw() on the sender’s account.

A deposit() on the recipient’s account.

The bank automatically logs the transaction in both accounts’ histories.

Any invalid inputs (e.g., non-existent accounts or insufficient balance) trigger custom exceptions such as InvalidAccountException or InsufficientFundsException.

Code Interaction:

Method: ATM.handleCrossCustomerTransfer(Customer customer)

Utilizes Bank.findAccountByNumber() to locate destination accounts efficiently (using a HashMap<String, BankAccount>).

Both accounts’ transaction histories (ArrayList<Transaction>) are updated accordingly.

3. Bank Administrator Interface
   3.1 Purpose

The Bank Administrator Interface introduces a new security context distinct from regular customers.
It provides administrative visibility and control over the system’s data.

3.2 Login Flow

The admin logs in using hardcoded credentials:

Username: admin

Password: password

Authentication is handled via a separate menu before entering the admin console.

3.3 Admin Functions
Function	Description	Method
View All Customers	Lists all registered customers along with their IDs and linked account numbers.	BankAdminInterface.viewAllCustomers()
View All Accounts	Displays all bank accounts with details like type, account number, customer ID, and balance.	BankAdminInterface.viewAllAccounts()
Create New Account	Allows the admin to create either a SavingsAccount or CheckingAccount for an existing customer.	BankAdminInterface.createAccount()
Unblock Account	Enables the admin to reset failed PIN attempts and restore access to blocked customers.	BankAdminInterface.unblockCustomer()
3.4 Interaction with the Bank Object

The BankAdministrator interface interacts with the Bank class directly to manage global data.

Aggregation:
The Bank class aggregates all customers and accounts in its data structures.
The admin console retrieves these via getters (getAllCustomers(), getAllAccounts()).

Encapsulation:
The Bank class remains the single source of truth. The admin cannot modify data arbitrarily but must use controlled methods (e.g., bank.createAccountForCustomer()).

Data Structures Used:

HashMap<String, Customer> → quick lookup by customer ID.

HashMap<String, BankAccount> → direct access to any account by its account number.

4. Polymorphism and Error Handling

Polymorphism:

BankAccount defines the general behavior for deposits and withdrawals.

SavingsAccount and CheckingAccount override these methods with specific constraints.

The ATM and Bank Admin interact with BankAccount references, relying on polymorphism for execution.

Custom Exceptions:

InsufficientFundsException → thrown when withdrawal exceeds limits.

InvalidAccountException → thrown if an entered account does not exist.

AccountBlockedException → thrown if login attempts exceed limits.

Robust Input Validation:

Scanner input is wrapped in try-catch blocks to handle invalid user input gracefully.

Logical validations (balance checks, existing IDs) prevent inconsistent data states.

5. Design Principles
   Principle	Description
   Separation of Concerns	ATM and Admin modules are completely isolated.
   Loose Coupling	Classes communicate through public interfaces; internal states are protected.
   High Cohesion	Each class performs a single, well-defined role.
   Extensibility	Adding new account types or operations requires minimal code modification.
   Maintainability	Clear logging, exception handling, and modular structure enhance readability.
6. Example Data Flow

Customer Transfer Flow (Cross-Customer):

Alice (Savings Account) → withdraws → Bob (Checking Account) → deposit.

Both transaction histories updated.

Success message + receipt displayed.

Admin Flow:

Admin logs in → views all accounts → creates new account for Carol → unblocks Bob’s account.

All changes logged internally by Bank.logEvent().

7. Future Extensions

Add Interest Calculation Scheduler for Savings Accounts.

Introduce Transaction Fees for Checking Accounts.

Persist data using file I/O or databases (e.g., serialization or JDBC).

Implement GUI-based ATM and Admin portals.

8. UML

+----------------+
|     Bank       |
+----------------+
| - customers: HashMap<String, Customer> |
| - accounts:  HashMap<String, BankAccount> |
+----------------+
| + registerCustomer(...)                  |
| + createAccountForCustomer(...)          |
| + findCustomerById(id): Customer         |
| + findAccountByNumber(accNo): BankAccount|
| + logEvent(event: String)                |
+----------------+
|
| aggregates
v
+----------------+
|   Customer     |
+----------------+
| - customerID: String  |
| - name: String         |
| - pin: String          |
| - accounts: ArrayList<BankAccount> |
| - failedAttempts: int  |
+----------------+
| + addAccount(account)  |
| + getAccounts()        |
| + verifyPIN(pin): bool |
+----------------+
|
| aggregates
v
+----------------------+
|    BankAccount       |  (abstract)
+----------------------+
| - accountNumber: String   |
| - balance: double          |
| - customerID: String       |
| - transactions: ArrayList<Transaction> |
+----------------------+
| + deposit(amount)          |
| + withdraw(amount)         |
| + getBalance()             |
| + addTransaction(tx)       |
+----------------------+
^                 ^
|                 |
|                 |
+-----------+   +----------------+
| Savings   |   | Checking       |
| Account   |   | Account        |
+-----------+   +----------------+
| - minBalance    | - overdraftLimit  |
| + withdraw()    | + withdraw()      |
| + interestCalc()| + applyFees()     |
+-----------+   +----------------+

+----------------+
|  Transaction   |
+----------------+
| - transactionID: String |
| - type: TransactionType |
| - amount: double        |
| - dateTime: LocalDateTime |
| - status: String        |
+----------------+

+----------------------+
|        ATM           |
+----------------------+
| + handleLogin()                    |
| + handleDeposit()                  |
| + handleWithdrawal()               |
| + handleIntraCustomerTransfer()    |
| + handleCrossCustomerTransfer()    |
| + printReceipt(Transaction)        |
+----------------------+

+-----------------------------+
|   BankAdminInterface        |
+-----------------------------+
| + login()                   |
| + viewAllCustomers()        |
| + viewAllAccounts()         |
| + createAccount()           |
| + unblockCustomer()         |
+-----------------------------+


9. Summary

The updated banking system is now a complete, modular, and extendable application.
It simulates both realistic customer interactions and bank-level administration, while maintaining clarity, data integrity, and OOP best practices.