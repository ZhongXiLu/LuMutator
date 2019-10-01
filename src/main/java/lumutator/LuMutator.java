package lumutator;

import lumutator.generator.AssertionGenerator;
import lumutator.parsers.pitest.PITest;
import lumutator.purity.PurityAnalyzer;
import lumutator.tracer.Tracer;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Main class of LuMutator (currently configured to work with PITest)
 * TODO: create general interface for other mutation tools?
 */
public class LuMutator {

    public static void main(String[] args) {
        try {
            Options options = new Options();

            Option configOption = new Option("c", "config", true, "Configuration file");
            configOption.setRequired(true);
            options.addOption(configOption);
            options.addOption(
                    new Option("m", "mutants", true, "PITest output directory (usually this is /target/pit-reports)")
            );
            options.addOption(
                    new Option("a", false, "Auto mode, automatically insert all new assertions in the original test files without asking first")
            );

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd;

            // Parse command line arguments
            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("LuMutator", options);
                System.exit(1);
                return;
            }

            // Parse configuration file
            // TODO: check if all required parameters are present
            Configuration.getInstance().initialize(cmd.getOptionValue("config"));
            Configuration config = Configuration.getInstance();

            // Compile project (main and tests)
            Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new RuntimeException("The test command failed; make sure all your tests pass");
            }

            // Purity Analysis
            PurityAnalyzer purityAnalyzer = new PurityAnalyzer();
            Set<String> inspectorMethods = purityAnalyzer.getInspectorMethods();

            // Trace the tests with original version
            JSONObject originalTrace = Tracer.trace(config.get("testDir"), inspectorMethods);

            // Parse mutations file
            List<Mutant> survivedMutants = PITest.getSurvivedMutants(
                    cmd.hasOption("mutations") ?
                            cmd.getOptionValue("mutations") :
                            Paths.get(config.get("projectDir"), "target", "pit-reports").toString()
            );

            // Longest step: trace every mutant and compare consequently
            List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons =
                    Tracer.traceAndCompareMutants(survivedMutants, originalTrace, inspectorMethods);

            // Generate the assertions based on the failed trace comparisons
            AssertionGenerator.generateAssertions(failedComparisons, !cmd.hasOption('a'));

        } catch (Exception e) {
            System.out.println("LuMutator caught an exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

    }
}
