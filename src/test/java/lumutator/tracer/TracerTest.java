package lumutator.tracer;

import lumutator.Mutant;
import lumutator.TestEnvironment;
import lumutator.parsers.pitest.PITest;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for the {@link Tracer}.
 * Testing on a simple Bank application (see /src/test/resources/bank).
 */
public class TracerTest extends TestEnvironment {

    /**
     * Trace of the tests of the original bank application.
     */
    private static JSONObject originalTrace;

    /**
     * Set up the trace.
     */
    @BeforeClass
    public static void setUp() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            originalTrace = Tracer.trace(classLoader.getResource("bank/src/test").getPath(), inspectorMethods);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the {@link Tracer#trace(String, Set)} method.
     */
    @Test
    public void testTrace() {
        // No need to check the contents of the trace, this is already done at lower level, see ObserverTest,
        // just make sure there is a trace for each test file.
        assertEquals(2, originalTrace.length());
    }

    /**
     * Test the {@link Tracer#traceAndCompareMutants(List, JSONObject, Set)} method.
     */
    @Test
    public void testTraceAndCompareMutants() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            List<Mutant> survivedMutants = PITest.getSurvivedMutants(classLoader.getResource("bank/pit-reports").getPath());

            List<JSONCompareResult> failedComparisons =
                    Tracer.traceAndCompareMutants(survivedMutants, originalTrace, inspectorMethods);

            // Three traces differ from the original trace
            assertEquals(3, failedComparisons.size());
            for (JSONCompareResult comparison : failedComparisons) {
                assertTrue(comparison.isFailureOnField());
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
