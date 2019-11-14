package lumutator;

import lumutator.generator.AssertionGenerator;
import lumutator.purity.PurityAnalyzer;
import lumutator.tracer.Tracer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Main class of LuMutator.
 */
public class LuMutator {

    /**
     * Generate assertions that kill survived mutants.
     *
     * @param configFile      Path to the configuration file for LuMutator.
     * @param survivedMutants List of all the survived mutants.
     * @param interactiveMode Ask the user to whether add the new assertions or not.
     * @throws IOException          If something went wrong with files.
     * @throws InterruptedException If an interrupt was issued.
     */
    public static void main(String configFile, List<Mutant> survivedMutants, boolean interactiveMode)
            throws IOException, InterruptedException {

        // Parse configuration file
        Configuration.getInstance().initialize(configFile);
        Configuration config = Configuration.getInstance();

        // Compile project (main and tests)
        Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new RuntimeException("The test command failed; make sure all your tests pass");
        }

        // Purity Analysis
        PurityAnalyzer purityAnalyzer = new PurityAnalyzer();
        Set<String> inspectorMethods = purityAnalyzer.getInspectorMethods(true);

        // Trace the tests with original version
        JSONObject originalTrace = Tracer.trace(config.get("testDir"), inspectorMethods);

        // Longest step: trace every mutant and compare consequently
        List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons =
                Tracer.traceAndCompareMutants(survivedMutants, originalTrace, inspectorMethods);

        // Generate the assertions based on the failed trace comparisons
        AssertionGenerator.generateAssertions(failedComparisons, interactiveMode);
    }
}
