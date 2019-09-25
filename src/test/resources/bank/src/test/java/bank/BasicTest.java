package bank;

import org.junit.Test;

/**
 * Some basic tests.
 * This is in fact used by another test (ObserverTest in the LuMutator project).
 */
public class BasicTest {

    /**
     * Test primitive types.
     */
    @Test
    public void testPrimitiveTypes() {
        boolean bool = true;
        byte aByte = 10;
        char character = 'b';
        double aDouble = 12.345;
        float aFloat = 1.1f;
        int aInt = 42;
        long aLong = 11111111;
        short aShort = 2;
    }

    /**
     * Test complex types.
     */
    @Test
    public void testComplexTypes() {
        String aString = "Hello World!";
        Object nullObject = null;
        //int[] intArray = {3, 2, 1, 0};
        // TODO: inspector method return object
    }
}
