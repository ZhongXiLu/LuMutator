package lumutator.tracer.debugger;

import lumutator.Configuration;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Tests for both the {@link Debugger} and {@link Observer}.
 * Testing on a simple Bank application (see /src/test/resources/bank).
 */
public class ObserverTest {

    /**
     * All the inspector methods from the Bank application.
     */
    private static final Set<String> inspectorMethods = new HashSet<>(Arrays.asList(
            "Customer.getName",
            "Customer.getAccountNumber",
            "Customer.getBalance"
    ));

    /**
     * Some initial configuration and setting the correct environment,
     */
    @BeforeClass
    public static void setUp() {
        ClassLoader classLoader = ObserverTest.class.getClassLoader();
        try {
            // Set up
            Configuration.getInstance().initialize(classLoader.getResource("bank_config.xml").getFile());
            Configuration config = Configuration.getInstance();

            // Small hack to get junit jar added to classpath
            String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String classpathEntry : classpathEntries) {
                if (classpathEntry.contains("junit")) {
                    config.set("classPath", config.get("classPath") + ":" + classpathEntry);
                }
            }

            Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
            process.waitFor();
        } catch (Exception e) {
            e.getStackTrace();
            fail();
        }
    }

    /**
     * Clean up the working directory (bank application).
     */
    @AfterClass
    public static void tearDown() {
        try {
            // Clean up
            Process process2 = Runtime.getRuntime().exec("mvn clean", null, new File(Configuration.getInstance().get("projectDir")));
            process2.waitFor();

        } catch (Exception e) {
            e.getStackTrace();
            fail();
        }
    }

    /**
     * Test if the JDI (primitive) types are correctly casted to the corresponding java types and can be found in the trace.
     */
    @Test
    public void testPrimitiveTypes() {
        // Set up
        Observer observer = new Observer(inspectorMethods);
        Debugger debugger = new Debugger("BasicTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testPrimitiveTypes");
        debugger.run();

        // Check the traces
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
            "{22:{character:98,aShort:2,aInt:42,bool:true,aLong:11111111,aFloat:1.1,aDouble:12.345,aByte:10},14:{},15:{bool:true},16:{bool:true,aByte:10},17:{character:98,bool:true,aByte:10},18:{character:98,bool:true,aDouble:12.345,aByte:10},19:{character:98,bool:true,aFloat:1.1,aDouble:12.345,aByte:10},20:{character:98,aInt:42,bool:true,aFloat:1.1,aDouble:12.345,aByte:10},21:{character:98,aInt:42,bool:true,aLong:11111111,aFloat:1.1,aDouble:12.345,aByte:10}}"
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
        Debugger debugger = new Debugger("BasicTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testComplexTypes");
        debugger.run();

        // Check the traces
        // TODO: inspector method return object
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
            "{29:{},30:{aString:Hello World!},33:{aString:Hello World!,nullObject:null}}"
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
        Debugger debugger = new Debugger("CustomerTest", observer);

        // Start the debugger
        debugger.addBreakpoint("testValidCustomers");
        debugger.run();

        // Check the traces
        JSONObject trace = observer.getTrace();
        JSONObject expectedTrace = new JSONObject(
            "{22:{exceptionThrown:false,customer1.getName():Jan Janssen,customer1.getBalance():100,customer1.getAccountNumber():091-0342401-48},26:{customer2.getName():Peter Selie,exceptionThrown:false,customer1.getName():Jan Janssen,customer1.getBalance():100,customer1.getAccountNumber():091-0342401-48,customer2.getAccountNumber():091-9871734-31,customer2.getBalance():777},17:{},28:{exceptionThrown:false},29:{exceptionThrown:false},21:{exceptionThrown:false}}"
        );
        JSONAssert.assertEquals(expectedTrace.toString(), trace.toString(), true);

        debugger.close();
    }

}
