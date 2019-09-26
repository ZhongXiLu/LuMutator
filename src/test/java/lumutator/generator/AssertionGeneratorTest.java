package lumutator.generator;

import lumutator.Mutant;
import lumutator.TestEnvironment;
import lumutator.parsers.pitest.PITest;
import lumutator.tracer.Tracer;
import lumutator.tracer.TracerTest;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link AssertionGenerator}.
 */
public class AssertionGeneratorTest extends TestEnvironment {

    /**
     * The failed comparisons for the bank application.
     */
    private static List<JSONCompareResult> failedComparisons;

    /**
     * Set up the failed comparisons.
     */
    @BeforeClass
    public static void setUp() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            JSONObject originalTrace = Tracer.trace(classLoader.getResource("bank/src/test").getPath(), inspectorMethods);
            List<Mutant> survivedMutants = PITest.getSurvivedMutants(classLoader.getResource("bank/pit-reports").getPath());

            failedComparisons = Tracer.traceAndCompareMutants(survivedMutants, originalTrace, inspectorMethods);
            assertEquals(3, failedComparisons.size());  // just to make sure

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the {@link AssertionGenerator#generateAssertions(List)} method.
     */
    @Test
    public void testGenerateAssertions() {
        try {
            AssertionGenerator.generateAssertions(failedComparisons);

            // TODO: check for changes in original test files

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}