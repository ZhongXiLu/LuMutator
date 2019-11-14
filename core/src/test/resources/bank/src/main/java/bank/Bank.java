package bank;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the bank in the bank application.
 */
public class Bank {

    /**
     * List of all the customers.
     */
    private List<Customer> customers;

    /**
     * Reference to last added customer.
     */
    private Customer lastAddedCustomer = null;

    /**
     * Create a new bank.
     */
    public Bank() {
        customers = new ArrayList<>();
    }

    /**
     * Check if a account number already exists in the bank.
     *
     * @param accountNr The account number to be checked.
     * @return True if already exists, otherwise false.
     */
    private boolean accountNrExists(String accountNr) {
        for (Customer otherCustomer : customers) {
            if (accountNr.equals(otherCustomer.getAccountNumber())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a new customer to the bank if they weren't already added.
     *
     * @param customer The new customer.
     */
    public void addCustomer(Customer customer) {
        if (!accountNrExists(customer.getAccountNumber())) {
            customers.add(customer);
            lastAddedCustomer = customer;
        }
    }

    /**
     * Get the customer that was last added to the bank.
     *
     * @return Last added customer.
     */
    public Customer getLastAddedCustomer() {
        return lastAddedCustomer;
    }

    /**
     * Get the customer id (index in the customers list) via the account number.
     *
     * @param accountNr The account number.
     * @return The customer id (-1 if  not found).
     */
    private int getCustomerId(String accountNr) {
        for (int i = 0; i < customers.size(); i++) {
            if (accountNr.equals(customers.get(i).getAccountNumber())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Make a transfer from account to another.
     *
     * @param fromAccountNr The account number to make the transfer from.
     * @param toAccountNr   The account number to make the transfer to.
     * @param amount        The amount to transfer.
     * @return True if the transfer went successfully, otherwise false.
     */
    public boolean internalTransfer(String fromAccountNr, String toAccountNr, int amount) {
        if (accountNrExists(fromAccountNr) && accountNrExists(toAccountNr) && amount > 0) {

            int fromCustomerId = getCustomerId(fromAccountNr);
            int toCustomerId = getCustomerId(toAccountNr);

            if (fromCustomerId != -1 && toCustomerId != -1) {

                Customer fromCustomer = customers.get(fromCustomerId);
                Customer toCustomer = customers.get(toCustomerId);

                if (fromCustomer.getBalance() >= amount) {
                    fromCustomer.setBalance(fromCustomer.getBalance() - amount);
                    toCustomer.setBalance(toCustomer.getBalance() + amount);
                    return true;
                }

            }
        }
        return false;
    }

}
