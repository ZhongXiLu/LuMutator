package lumutator;

import lumutator.parsers.pitest.PITest;
import lumutator.purity.PurityAnalyzer;
import lumutator.tracer.Tracer;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Main class of LuMutator (currently configured to work with PITest)
 * TODO: create general interface for other mutation tools?
 */
public class LuMutator {

    public static void main(String args[]) {
        try {
            Options options = new Options();

            Option configOption = new Option("c", "config", true, "Configuration file");
            configOption.setRequired(true);
            options.addOption(configOption);

            Option mutations = new Option("m", "mutants", true, "PITest output directory (usually this is /target/pit-reports)");
            options.addOption(mutations);

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

            // TODO: add progress(bar)?

            // Parse configuration file
            try {
                Configuration.getInstance().initialize(cmd.getOptionValue("config"));
            } catch (Exception e) {
                e.getMessage();
                System.exit(1);
                return;
            }
            Configuration config = Configuration.getInstance();

            // Parse mutations file
            PITest.getMutants(
                cmd.hasOption("mutations") ?
                    cmd.getOptionValue("mutations") :
                    Paths.get(config.get("projectDir"), "target", "pit-reports").toString()
            );

            // Compile project (main and tests)
            Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
            process.waitFor();

            // Purity Analysis
            PurityAnalyzer purityAnalyzer = new PurityAnalyzer();
            Set<String> inspectorMethods = purityAnalyzer.getInspectorMethods();

            // Trace the tests
            Tracer.trace(config.get("testDir"), inspectorMethods);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
