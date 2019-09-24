package lumutator.tracer.debugger;

import lumutator.Configuration;
import org.json.JSONObject;
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
     * Test an entire debug run and check the trace from the observer.
     */
    @Test
    public void testObserver() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            // Set up
            Observer observer = new Observer(inspectorMethods);
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
            Debugger debugger = new Debugger("CustomerTest", observer);

            // Start the debugger
            debugger.addBreakpoint("testValidCustomers");
            debugger.run();

            // Check the traces
            JSONObject trace = observer.getTrace();
            JSONObject expectedTrace = new JSONObject(
                    "{22:{exceptionThrown:false,customer1.getName():\"Jan Janssen\",customer1.getBalance():100,customer1.getAccountNumber():\"091-0342401-48\"},26:{customer2.getName():\"Peter Selie\",exceptionThrown:false,customer1.getName():\"Jan Janssen\",customer1.getBalance():100,customer1.getAccountNumber():\"091-0342401-48\",customer2.getAccountNumber():\"091-9871734-31\",customer2.getBalance():777},17:{},28:{exceptionThrown:false},21:{exceptionThrown:false}}"
            );
            JSONAssert.assertEquals(expectedTrace.toString(), trace.toString(), false);

            // Clean up
            Process process2 = Runtime.getRuntime().exec("mvn clean", null, new File(config.get("projectDir")));
            process2.waitFor();

        } catch (Exception e) {
            e.getStackTrace();
            fail();
        }
    }

}
