package lumutator.generator;

import lumutator.Mutant;
import lumutator.TestEnvironment;
import lumutator.parsers.pitest.PITest;
import lumutator.tracer.Tracer;
import lumutator.tracer.TracerTest;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private static List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons;

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
            assertEquals(5, failedComparisons.size());  // just to make sure

        } catch (IOException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Save copy of original file since we are modifying it.
     */
    @Before
    public void setUpBeforeTest() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            Path originalFile = Paths.get(classLoader.getResource("bank/src/test/java/bank/BankTest.java").getPath());
            Files.copy(originalFile, Paths.get(originalFile.toString() + ".tmp"), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Restore original file.
     */
    @After
    public void tearDownAfterTest() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            Path originalFile = Paths.get(classLoader.getResource("bank/src/test/java/bank/BankTest.java").getPath());
            Files.move(Paths.get(originalFile.toString() + ".tmp"), originalFile, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link AssertionGenerator#generateAssertions(List)} method.
     */
    @Test
    public void testGenerateAssertions() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            Path originalFile = Paths.get(classLoader.getResource("bank/src/test/java/bank/BankTest.java").getPath());

            AssertionGenerator.generateAssertions(failedComparisons);

            final List<String> expectedAssertions = Arrays.asList(
                    "assertEquals(100, bank.getLastAddedCustomer().getBalance());",
                    "assertEquals(\"Jan Janssen\", bank.getLastAddedCustomer().getName());",
                    "assertEquals(0, customer1.getBalance());",
                    "assertEquals(true, success);"
            );
            List<String> lines = Files.readAllLines(originalFile);

            // Expect a new assertion added on following lines
            assertTrue(expectedAssertions.contains(lines.get(45).trim()));
            assertTrue(expectedAssertions.contains(lines.get(46).trim()));
            assertTrue(expectedAssertions.contains(lines.get(63).trim()));
            assertTrue(expectedAssertions.contains(lines.get(64).trim()));

        } catch (IOException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link AssertionGenerator#generateAssertions(List, boolean)} method with user interaction.
     */
    @Test
    public void testGenerateAssertionsWithInteraction() {
        try {
            ClassLoader classLoader = TracerTest.class.getClassLoader();
            Path originalFile = Paths.get(classLoader.getResource("bank/src/test/java/bank/BankTest.java").getPath());
            int originalLineCount = Files.readAllLines(originalFile).size();

            // Test with NO as user input
            System.setIn(new ByteArrayInputStream("NO\n".getBytes()));
            AssertionGenerator.generateAssertions(failedComparisons.subList(0, 1), true);

            // Just check if a line is added or not, the contents are already checked in `testGenerateAssertions`
            assertEquals(originalLineCount, Files.readAllLines(originalFile).size());  // nothing changed

            // Test with YES as user input
            System.setIn(new ByteArrayInputStream("YES\n".getBytes()));
            AssertionGenerator.generateAssertions(failedComparisons.subList(1, 2), true);

            // Just check if a line is added or not, the contents are already checked in `testGenerateAssertions`
            assertEquals(originalLineCount + 1, Files.readAllLines(originalFile).size());  // one extra line

        } catch (IOException e) {
            // Should not be possible
            fail();
        }
    }
}