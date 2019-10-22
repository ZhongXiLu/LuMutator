package lumutator.tracer.debugger;

import lumutator.TestEnvironment;
import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Tests for both the {@link Debugger} and {@link Observer}.
 * Testing on a simple Bank application (see /src/test/resources/bank).
 */
public class ObserverTest extends TestEnvironment {

    /**
     * Test if the JDI (primitive) types are correctly casted to the corresponding java types and can be found in the trace.
     */
    @Test
    public void testPrimitiveTypes() {
        // Set up
        Observer observer = new Observer(inspectorMethods);
        Debugger debugger = new Debugger("bank.BasicTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testPrimitiveTypes");
        debugger.run();

        // Check the traces
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
                "{22:{character:98,aInt:42,bool:true,aFloat:1.1,aDouble:12.345,aByte:10},23:{character:98,aInt:42,bool:true,aLong:11111111,aFloat:1.1,aDouble:12.345,aByte:10},24:{character:98,aShort:2,aInt:42,bool:true,aLong:11111111,aFloat:1.1,aDouble:12.345,aByte:10},17:{bool:true},18:{bool:true,aByte:10},19:{character:98,bool:true,aByte:10},20:{character:98,bool:true,aDouble:12.345,aByte:10},21:{character:98,bool:true,aFloat:1.1,aDouble:12.345,aByte:10}}"
        );
        JSONAssert.assertEquals(expectedTrace.toString(), trace.toString(), true);

        debugger.close();
    }

    /**
     * Test if the JDI complex types (derived from {@link com.sun.jdi.ObjectReference}) are correctly casted to
     * the corresponding java types and can be found in the trace.
     */
    @Test
    public void testComplexTypes() {
        // Set up
        Observer observer = new Observer(inspectorMethods);
        Debugger debugger = new Debugger("bank.BasicTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testComplexTypes");
        debugger.run();

        // Check the traces
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
                "{33:{aString:Hello World!,nullObject:null},34:{\"intArray[3]\":0,\"intArray[2]\":1,aString:Hello World!,\"intArray[1]\":2,\"intArray[0]\":3,nullObject:null},32:{aString:Hello World!}}"
        );
        JSONAssert.assertEquals(expectedTrace.toString(), trace.toString(), true);

        debugger.close();
    }

    /**
     * Test if inspector methods are evaluated and if the results of it are stored in the trace.
     */
    @Test
    public void testInspectorMethods() {
        // Set up
        Observer observer = new Observer(inspectorMethods);
        Debugger debugger = new Debugger("bank.CustomerTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testValidCustomers");
        debugger.run();

        // Check the traces
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
            "{23:{exceptionThrown:false},24:{exceptionThrown:false,customer1.getName():Jan Janssen,customer1.getBalance():100,customer1.getAccountNumber():091-0342401-48},28:{customer2.getName():Peter Selie,exceptionThrown:false,customer1.getName():Jan Janssen,customer1.getBalance():100,customer1.getAccountNumber():091-0342401-48,customer2.getAccountNumber():091-9871734-31,customer2.getBalance():777},30:{exceptionThrown:false},31:{exceptionThrown:false}}"
        );
        JSONAssert.assertEquals(expectedTrace.toString(), trace.toString(), true);

        debugger.close();
    }

    /**
     * Test if inspector methods are evaluated and if the results of it are stored in the trace.
     * This time recursively, i.e. an inspector method returns another complex object.
     */
    @Test
    public void testInspectorMethodsRecursive() {
        // Set up
        Observer observer = new Observer(inspectorMethods);
        Debugger debugger = new Debugger("bank.BankTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testAddingCustomers");
        debugger.run();

        // Check the traces
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
            "{45:{customer2.getName():Peter Selie,bank.getLastAddedCustomer().getName():Jan Janssen,customer1.getName():Jan Janssen,customer1.getBalance():100,bank.getLastAddedCustomer().getBalance():100,bank.getLastAddedCustomer().getAccountNumber():091-0342401-48,customer1.getAccountNumber():091-0342401-48,customer2.getAccountNumber():091-9871734-31,customer2.getBalance():777},46:{customer2.getName():Peter Selie,bank.getLastAddedCustomer().getName():Peter Selie,customer1.getName():Jan Janssen,customer1.getBalance():100,bank.getLastAddedCustomer().getBalance():777,bank.getLastAddedCustomer().getAccountNumber():091-9871734-31,customer1.getAccountNumber():091-0342401-48,customer2.getAccountNumber():091-9871734-31,customer2.getBalance():777},47:{customer2.getName():Peter Selie,bank.getLastAddedCustomer().getName():Peter Selie,customer1.getName():Jan Janssen,customer1.getBalance():100,bank.getLastAddedCustomer().getBalance():777,bank.getLastAddedCustomer().getAccountNumber():091-9871734-31,customer1.getAccountNumber():091-0342401-48,customer2.getAccountNumber():091-9871734-31,customer2.getBalance():777}}"
        );
        JSONAssert.assertEquals(expectedTrace.toString(), trace.toString(), true);

        debugger.close();
    }

}
