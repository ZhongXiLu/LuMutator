package bank;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the {@link Bank}.
 */
public class BankTest {

    /**
     * Bank to test.
     */
    private Bank bank;

    /**
     * A customer who is already added to the bank.
     */
    private Customer customer1;

    /**
     * A customer who is NOT added to the bank.
     */
    private Customer customer2;

    /**
     * Set up the bank with one customer already in it
     * and set up another customer who is yet to be added.
     */
    @Before
    public void setUp() {
        bank = new Bank();
        customer1 = new Customer("Jan Janssen", "091-0342401-48", 100);
        customer2 = new Customer("Peter Selie", "091-9871734-31", 777);
        bank.addCustomer(customer1);
    }

    /**
     * Test adding a customer.
     */
    @Test
    public void testAddingCustomers() {
        bank.addCustomer(customer2);
        assertNotNull(bank.getLastAddedCustomer());
    }

    /**
     * Test a transfer.
     */
    @Test
    public void testTransfer() {
        bank.addCustomer(customer2);

        int balanceCustomer1 = customer1.getBalance();
        int balanceCustomer2 = customer2.getBalance();

        boolean success = bank.internalTransfer(customer1.getAccountNumber(), customer2.getAccountNumber(), balanceCustomer1);

        assertEquals(balanceCustomer1 + balanceCustomer2, customer2.getBalance());
    }

}