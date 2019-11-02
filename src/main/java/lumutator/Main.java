package lumutator;

import lumutator.parsers.pitest.PITest;
import org.apache.commons.cli.*;

import java.nio.file.Paths;
import java.util.List;

/**
 * Example of a main that uses LuMutator.
 * In this example, PITest was used to generate the survived mutants.
 *
 * If you want to use another mutation tool, all you have to do is change the `PITest.getSurvivedMutants`
 * so it will parse the reports created by the other tool and make sure it returns a list of survived mutants.
 */
public class Main {

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

            // Explicitly parse the configuration, since some of the values are needed to parse the PITest mutations
            Configuration.getInstance().initialize(cmd.getOptionValue("config"));
            Configuration config = Configuration.getInstance();

            // Parse mutations file from PITest
            List<Mutant> survivedMutants = PITest.getSurvivedMutants(
                    cmd.hasOption("mutants") ?
                            cmd.getOptionValue("mutants") :
                            Paths.get(config.get("projectDir"), "target", "pit-reports").toString()
            );

            // Call LuMutator
            LuMutator.main(cmd.getOptionValue("config"), survivedMutants, !cmd.hasOption('a'));

        } catch (Exception e) {
            System.out.println("LuMutator caught an exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
