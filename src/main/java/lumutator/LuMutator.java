package lumutator;

import lumutator.parsers.pitest.PITest;
import lumutator.purity.PurityAnalyzer;
import lumutator.tracer.Tracer;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class LuMutator {

    public static void main(String args[]) {
        try {
            Options options = new Options();

            Option configOption = new Option("c", "config", true, "Configuration file");
            configOption.setRequired(true);
            options.addOption(configOption);

            Option mutations = new Option("m", "mutations", true, "Mutations file");
            mutations.setRequired(true);
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
            Configuration config;
            try {
                config = new Configuration(cmd.getOptionValue("config"));
            } catch (Exception e) {
                e.getMessage();
                System.exit(1);
                return;
            }

            // Parse mutations file
            PITest piTest = new PITest(cmd.getOptionValue("mutations"));

            // Set working directory
            System.setProperty("user.dir", config.get("projectDir"));

            // Compile project (main and tests)
            try {
                Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
                process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Purity Analysis
            PurityAnalyzer purityAnalyzer = new PurityAnalyzer(config);
            Set<String> inspectorMethods = purityAnalyzer.getInspectorMethods();

            // Trace the tests
            List<File> testFiles = (List<File>) FileUtils.listFiles(
                    new File(config.get("testDir")),
                    new RegexFileFilter("(?i)^(.*?test.*?)"),       // only match test files
                    DirectoryFileFilter.DIRECTORY
            );
            Tracer.trace(config, testFiles, inspectorMethods);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
