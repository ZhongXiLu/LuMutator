package lumutator.purity;

import lumutator.Configuration;
import org.opalj.support.info.Purity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Purity analyzer with OPIUM (http://www.opal-project.de/Opium.html).
 * Also includes a parser of the generated results file.
 */
public class PurityAnalyzer {

    /**
     * Run a purity analysis.
     */
    public PurityAnalyzer() {
        final String[] arguments = new String[]{
                "-cp", Configuration.getInstance().get("classFiles"),   // path to directory with .class files
                "-individual",                                          // print result of each method
                "-eval", "."                                            // output directory for the results
        };
        Purity.main(arguments);
    }

    /**
     * Get the name of the method (including package's name) of a line in the results file of the analysis.
     *
     * @param line The line in the results file.
     * @return Name of the method.
     */
    static private String getMethodName(String line) {
        final String packageName = line.split("\\{")[0];

        String methodName = "";
        Matcher matcher = Pattern.compile(" ([^( ]+)\\(").matcher(line);
        if (matcher.find()) {
            methodName = matcher.group(1);
        }

        return packageName + "." + methodName;
    }

    /**
     * Get the purity result of the method.
     *
     * @param line The line in the results file.
     * @return The purity result.
     */
    static private String getPurityResult(String line) {
        Matcher matcher = Pattern.compile("=> (.+)").matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Get the parameters of the method.
     *
     * @param line The line in the results file.
     * @return The parameters of the method, empty string if no parameters.
     */
    static private String getMethodParameters(String line) {
        Matcher matcher = Pattern.compile("\\((.+)\\)").matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Get the return type of the method.
     *
     * @param line The line in the results file.
     * @return The return type.
     */
    static private String getReturnType(String line) {
        Matcher matcher = Pattern.compile("([^ ]+?) ([^ ]+)\\(").matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }


    /**
     * Parse a line from the results file from the analysis
     * and return the inspector method's name (and package) if it exists.
     *
     * @param line A line from the file.
     * @return Name of the inspector method, {@code null} if it was not an inspector method.
     */
    static private String getInspectorMethodIfExists(String line) {
        // TODO: everything but impure?
        if (!getPurityResult(line).equals("impure") && getMethodParameters(line).equals("")
                && !getReturnType(line).equals("void")) {
            return getMethodName(line);
        }
        return "";
    }

    /**
     * Get all the inspector methods from the purity analysis.
     *
     * @return Set of all the inspector methods.
     * @throws IOException If the analysis wasn't successful.
     */
    public Set<String> getInspectorMethods() throws IOException {
        Set<String> inspectorMethods = new HashSet<>();

        final String outputDir = new File(Configuration.getInstance().get("classFiles")).getName();
        for (String line : Files.readAllLines(Paths.get(outputDir, "method-results.csv"))) {
            String method = getInspectorMethodIfExists(line);
            if (!method.equals("")) {
                inspectorMethods.add(method);
            }
        }

        return inspectorMethods;
    }

}
