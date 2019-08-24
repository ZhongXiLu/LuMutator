/**
 * Represents a customer in the bank application.
 */
public class Customer {

    /**
     * Name of the customer.
     */
    private String name;

    /**
     * Number of the account.
     */
    private String accountNumber;

    /**
     * Current balance on the account.
     */
    private int balance;

    /**
     * Create a new customer.
     *
     * @param name          Name of the customer.
     * @param accountNumber Number of the account.
     */
    public Customer(String name, String accountNumber, int balance) {

        // Check balance
        if (balance < 0) {
            throw new IllegalArgumentException("Balance must be >= 0");
        }

        // Check if valid account number.
        if (accountNumber.length() != 14) {
            throw new IllegalArgumentException("Account number must be 14 characters long");
        } else if (!accountNumber.substring(0, 3).equals("091")) {
            throw new IllegalArgumentException("The first three characters must be '091'");
        } else if (accountNumber.charAt(3) != '-' || accountNumber.charAt(11) != '-') {
            throw new IllegalArgumentException("Account number is not correctly separated by '-'");
        } else if (Integer.parseInt(accountNumber.substring(0, 3) + accountNumber.substring(4, 7)) % 97 != Integer.parseInt(accountNumber.substring(12, 14))) {
            throw new IllegalArgumentException("Control numbers are wrong");
        }

        this.name = name;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    /**
     * Get the name of the customer.
     *
     * @return Name of the customer.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the account number.
     *
     * @return The account number.
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     * Get the current balance on the account.
     *
     * @return The current balance on the account.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * Set the balance on the account.
     *
     * @param balance The new balance.
     */
    public void setBalance(int balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance must be >= 0");
        }
        this.balance = balance;
    }
}
