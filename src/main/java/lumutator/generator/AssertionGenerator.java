package lumutator.generator;

import org.skyscreamer.jsonassert.FieldComparisonFailure;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.util.List;

/**
 * AssertionGenerator: generates assertions based on the trace comparisons.
 */
public class AssertionGenerator {

    /**
     * Generate all possible assertions based on the failed trace comparisons
     * and directly insert them in the original test files.
     *
     * @param failedComparisons List of the failed trace comparisons.
     */
    public static void generateAssertions(List<JSONCompareResult> failedComparisons) {
        generateAssertions(failedComparisons, false);
    }

    /**
     * Generate all possible assertions based on the failed trace comparisons
     * and possibly ask the user whether to insert them or not in the original test files.
     *
     * @param failedComparisons List of the failed trace comparisons.
     * @param interactiveMode   Ask the user to whether add the new assertions or not.
     */
    public static void generateAssertions(List<JSONCompareResult> failedComparisons, boolean interactiveMode) {

        for (JSONCompareResult comparison : failedComparisons) {
            for (FieldComparisonFailure diff : comparison.getFieldFailures()) {
                //System.out.println(diff.getField());

                // Only consider one assertion per mutant, this is likely enough to kill the mutant.
                break;
            }
        }

    }
}
