package bank;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * Tests for the {@link Customer}.
 */
public class CustomerTest {

    /**
     * Test valid customers.
     */
    @Test
    public void testValidCustomers() {
        Customer customer1;
        Customer customer2;
        boolean exceptionThrown = false;

        try {

            customer1 = new Customer("Jan Janssen", "091-0342401-48", 100);
            customer2 = new Customer("Peter Selie", "091-9871734-31", 777);

        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }

        assertFalse(exceptionThrown);
    }

    // No tests for other methods! :(
}
