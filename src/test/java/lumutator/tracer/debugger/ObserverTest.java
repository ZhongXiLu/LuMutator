package lumutator.tracer.debugger;

import lumutator.Configuration;
import org.json.JSONArray;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

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
            JSONArray trace = observer.getTrace();
            assertEquals(5, trace.length());
            assertEquals("{\"17\":[]}", trace.get(0).toString());
            assertEquals("{\"21\":[{\"exceptionThrown\":\"false\"}]}", trace.get(1).toString());

            // Manual check one of the others, since order is NOT preserved, so we cannot compare strings
            final List<String> expectedTraces = Arrays.asList(
                    "{customer1.getName()=\"Jan Janssen\"}",
                    "{customer1.getAccountNumber()=\"091-0342401-48\"}",
                    "{customer1.getBalance()=100}",
                    "{customer2.getName()=\"Peter Selie\"}",
                    "{customer2.getAccountNumber()=\"091-9871734-31\"}",
                    "{customer2.getBalance()=777}",
                    "{exceptionThrown=false}"
            );
            JSONArray traceOnLine22 = trace.getJSONObject(3).getJSONArray("26");
            for (Object obj : traceOnLine22.toList()) {
                assertTrue(expectedTraces.contains(obj.toString()));
            }

            // Clean up
            Process process2 = Runtime.getRuntime().exec("mvn clean", null, new File(config.get("projectDir")));
            process2.waitFor();

        } catch (Exception e) {
            e.getStackTrace();
            fail();
        }
    }

}
