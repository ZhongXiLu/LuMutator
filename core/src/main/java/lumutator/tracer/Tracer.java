package lumutator.tracer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import lumutator.Configuration;
import lumutator.Mutant;
import lumutator.tracer.debugger.Debugger;
import lumutator.tracer.debugger.Observer;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Tracer: trace a set of tests.
 */
public abstract class Tracer {

    /**
     * All the JUnit annotations for the "tests".
     */
    private static final Set<String> junitAnnotations = new HashSet<>(Arrays.asList(
            "Test", "Before", "After", "BeforeClass", "AfterClass"
    ));

    /**
     * Trace a set of tests.
     *
     * @param directory        Directory that contains the test files that need to be traced.
     * @param inspectorMethods Set of all inspector methods in the source classes.
     * @return The traces of the tests.
     */
    public static JSONObject trace(String directory, Set<String> inspectorMethods) {
        List<File> testFiles = (List<File>) FileUtils.listFiles(
                new File(directory),
                new RegexFileFilter("(?i)^(.*?test.*?)"),       // only match test files
                DirectoryFileFilter.DIRECTORY
        );

        JSONObject traces = new JSONObject();
        for (File file : testFiles) {
            try {
                CompilationUnit compilationUnit = JavaParser.parse(file);
                final String classToDebug = String.format(
                        "%s.%s",
                        compilationUnit.getPackage().getName(),
                        FilenameUtils.removeExtension(file.getName())
                );

                Observer observer = new Observer(inspectorMethods);
                Debugger debugger = new Debugger(classToDebug, observer);

                // Set breakpoint at start of each test (@Test)
                for (TypeDeclaration decl : compilationUnit.getTypes()) {
                    for (BodyDeclaration member : decl.getMembers()) {
                        for (AnnotationExpr annotation : member.getAnnotations()) {
                            if (junitAnnotations.contains(annotation.getName().toString())) {
                                MethodDeclaration field = (MethodDeclaration) member;
                                debugger.addBreakpoint(field.getName());
                            }
                        }
                    }
                }

                debugger.run();
                debugger.close();
                traces.put(file.getCanonicalPath(), observer.getTrace());

            } catch (ParseException | IOException e) {
                // Should not be possible
            }
        }

        return traces;
    }

    /**
     * Trace all the mutants tests and also compare them afterwards with the original trace.
     *
     * @param survivedMutants  List of all the survived mutants that need to be traced.
     * @param originalTrace    The trace from the original version of the code.
     * @param inspectorMethods Set of all inspector methods in the source classes.
     * @return List of all the failed trace comparisons between the original and mutant trace (consists of the json comparison and the associated mutant).
     */
    public static List<ImmutablePair<JSONCompareResult, Mutant>> traceAndCompareMutants(
            List<Mutant> survivedMutants, JSONObject originalTrace, Set<String> inspectorMethods) {

        List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons = new ArrayList<>();
        Configuration config = Configuration.getInstance();

        try {
            String currentTempFile = "";    // Store current class, so we dont need to make a copy for each mutant
            for (Mutant mutant : ProgressBar.wrap(survivedMutants, "Tracing Mutants")) {
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

                JSONObject mutantTrace = trace(config.get("testDir"), inspectorMethods);

                // Compare traces
                // LENIENT is fastest and we dont need strictness or extensibility checks
                JSONCompareResult comparison = JSONCompare.compareJSON(originalTrace, mutantTrace, JSONCompareMode.LENIENT);
                if (comparison.isFailureOnField()) {
                    failedComparisons.add(new ImmutablePair<>(comparison, mutant));
                }
            }
            // Restore copy of class of last mutant
            Files.move(Paths.get(currentTempFile), Paths.get(currentTempFile.replace(".tmp", ".class")), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException e) {
            // Should not be possible
        }

        return failedComparisons;
    }

}
