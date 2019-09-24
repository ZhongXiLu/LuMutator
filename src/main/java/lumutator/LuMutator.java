package lumutator;

import lumutator.parsers.pitest.PITest;
import lumutator.purity.PurityAnalyzer;
import lumutator.tracer.Tracer;
import org.apache.commons.cli.*;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
                // TODO: check if all required parameters are present
            } catch (Exception e) {
                e.getMessage();
                System.exit(1);
                return;
            }
            Configuration config = Configuration.getInstance();

            // Compile project (main and tests)
            Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
            process.waitFor();

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

            // TODO: encapsulate code below
            String currentTempFile = "";    // Store current class, so we dont need to make a copy for each mutant
            for (Mutant mutant : survivedMutants) {
                final String classFilesDir = config.get("classFiles") + "/" + mutant.getMutatedClass().replace(".", "/");
                final String newClassFile = mutant.getClassFile().getCanonicalPath();
                final String oldClassFile = classFilesDir + ".class";
                final String oldClassTempFile = classFilesDir + ".tmp";

                // Create copy of original .class file if necessary
                if (currentTempFile.isEmpty()) {
                    // Start
                    Files.move(Paths.get(oldClassFile), Paths.get(oldClassTempFile), StandardCopyOption.REPLACE_EXISTING);
                    currentTempFile = oldClassTempFile;
                } else if (!currentTempFile.equals(oldClassTempFile)) {
                    // New class
                    Files.move(Paths.get(currentTempFile), Paths.get(currentTempFile.replace(".tmp", ".class")), StandardCopyOption.REPLACE_EXISTING);
                    Files.move(Paths.get(oldClassFile), Paths.get(oldClassTempFile), StandardCopyOption.REPLACE_EXISTING);
                    currentTempFile = oldClassTempFile;
                } // else: Mutants still in same class, no need to make copy of original

                // Copy the mutant .class file
                Files.copy(Paths.get(newClassFile), Paths.get(oldClassFile), StandardCopyOption.REPLACE_EXISTING);

                JSONObject mutantTrace = Tracer.trace(config.get("testDir"), inspectorMethods);

                // TODO: compare traces
            }
            // Restore copy of class of last mutant
            Files.move(Paths.get(currentTempFile), Paths.get(currentTempFile.replace(".tmp", ".class")), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
