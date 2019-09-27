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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

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
            // Save copy of original file since we are modifying it
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            Path originalFile = Paths.get(classLoader.getResource("bank/src/test/java/bank/CustomerTest.java").getPath());
            Files.copy(originalFile, Paths.get(originalFile.toString() + ".tmp"), StandardCopyOption.REPLACE_EXISTING);

            AssertionGenerator.generateAssertions(failedComparisons);

            // TODO: check for changes in original test files
            final List<String> expectedAssertions = Arrays.asList(
                    "assertEquals(\"091-0342401-48\", customer1.getAccountNumber());",
                    "assertEquals(100, customer1.getBalance());",
                    "assertEquals(\"Jan Janssen\", customer1.getName());"
            );
            List<String> lines = Files.readAllLines(originalFile);
            assertTrue(expectedAssertions.contains(lines.get(24).trim()));
            assertTrue(expectedAssertions.contains(lines.get(25).trim()));
            assertTrue(expectedAssertions.contains(lines.get(26).trim()));

            // Restore original file
            Files.move(Paths.get(originalFile.toString() + ".tmp"), originalFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}