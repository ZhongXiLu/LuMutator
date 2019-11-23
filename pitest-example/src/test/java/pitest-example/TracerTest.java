package pitest;

import lumutator.Mutant;
import lumutator.tracer.Tracer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for the {@link Tracer}.
 * Testing on a simple Bank application (see /src/test/resources/bank).
 */
public class TracerTest extends pitest.TestEnvironment {

    /**
     * Trace of the tests of the original bank application.
     */
    private static JSONObject originalTrace;

    /**
     * Set up the trace.
     */
    @BeforeClass
    public static void setUp() {
        ClassLoader classLoader = TracerTest.class.getClassLoader();
        originalTrace = Tracer.trace(classLoader.getResource("bank/src/test").getPath(), inspectorMethods);
    }

    /**
     * Test the {@link Tracer#trace(String, Set)} method.
     */
    @Test
    public void testTrace() {
        // No need to check the contents of the trace, this is already done at lower level, see ObserverTest,
        // just make sure there is a trace for each test file.
        assertEquals(3, originalTrace.length());
    }

    /**
     * Test the {@link Tracer#traceAndCompareMutants(List, JSONObject, Set)} method.
     */
    @Test
    public void testTraceAndCompareMutants() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            List<Mutant> survivedMutants = pitest.Parser.getMutants(classLoader.getResource("bank/pit-reports").getPath(), true);

            List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons =
                    Tracer.traceAndCompareMutants(survivedMutants, originalTrace, inspectorMethods);

            // Three traces differ from the original trace
            assertEquals(5, failedComparisons.size());
            for (ImmutablePair<JSONCompareResult, Mutant> comparison : failedComparisons) {
                assertTrue(comparison.getKey().isFailureOnField());
            }

        } catch (IOException e) {
            // Should not be possible
            fail();
        }
    }

}
