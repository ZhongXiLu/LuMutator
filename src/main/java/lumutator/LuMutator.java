package lumutator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lumutator.debugger.Debugger;
import lumutator.parsers.pitest.PITest;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.List;

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

            // Iterate over test files
            List<File> files = (List<File>) FileUtils.listFiles(
                    new File(config.get("testDir")),
                    new RegexFileFilter("(?i)^(.*?test.*?)"),       // only match test files
                    DirectoryFileFilter.DIRECTORY
            );

            for (File file : files) {
                try {
                    File directory = new File("traces");
                    directory.deleteOnExit();   // TODO: fix this?
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

                    CompilationUnit compilationUnit = JavaParser.parse(file);
                    String classToDebug = String.format(
                            "%s.%s",
                            compilationUnit.getPackage().getName(),
                            FilenameUtils.removeExtension(file.getName())
                    );

                    Debugger debugger = new Debugger(config, classToDebug);

                    // Set breakpoint at start of each test (@Test)
                    for (TypeDeclaration decl : compilationUnit.getTypes()) {
                        for (BodyDeclaration member : decl.getMembers()) {
                            for (AnnotationExpr annotation : member.getAnnotations()) {
                                if (annotation.getName().toString().equals("Test")) {   // TODO: also include @Before, ...?
                                    MethodDeclaration field = (MethodDeclaration) member;
                                    debugger.addBreakpoint(field.getName());
                                }
                            }
                        }
                    }

                    debugger.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
