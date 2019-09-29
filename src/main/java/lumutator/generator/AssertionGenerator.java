package lumutator.generator;

import lumutator.Mutant;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.skyscreamer.jsonassert.FieldComparisonFailure;
import org.skyscreamer.jsonassert.JSONCompareResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static void generateAssertions(List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons) throws IOException {
        generateAssertions(failedComparisons, false);
    }

    /**
     * Generate all possible assertions based on the failed trace comparisons
     * and possibly ask the user whether to insert them or not in the original test files.
     *
     * @param failedComparisons List of the failed trace comparisons.
     * @param interactiveMode   Ask the user to whether add the new assertions or not.
     */
    public static void generateAssertions(List<ImmutablePair<JSONCompareResult, Mutant>> failedComparisons, boolean interactiveMode)
            throws IOException {

        // Keep track in which file and on what line nr we have added new lines
        HashMap<String, List<Integer>> insertionInformation = new HashMap<>();

        for (ImmutablePair<JSONCompareResult, Mutant> comparison : failedComparisons) {
            for (FieldComparisonFailure diff : comparison.getKey().getFieldFailures()) {
                String[] parts = diff.getField().split("\\.java\\.");
                String[] parts2 = parts[1].split("\\.", 2);

                final Path testFile = Paths.get(parts[0] + ".java");
                final int lineNr = Integer.parseInt(parts2[0]);

                // Initialize list in hashmap if necessary
                if (!insertionInformation.containsKey(testFile.toString())) {
                    insertionInformation.put(testFile.toString(), new ArrayList<>());
                }
                // Since we are inserting new lines in files, we have to adjust the original line nr.
                // Example: if we added a new line on line nr 12 and afterwards, we want to add
                //          another line, but this time on line nr 36, we have to adjust this to line nr 37
                //          since there was one additional line added
                // To adjust the line nr, simply check how many new lines have been added previously
                // and add the amount of new lines to the original line nr
                final int adjustedLineNr = lineNr + Math.toIntExact(
                        insertionInformation.get(testFile.toString()).stream().filter(l -> l < lineNr).count()
                );

                List<String> lines = Files.readAllLines(testFile);
                final String assertion = String.format(
                        "%sassertEquals(%s, %s);",
                        getIndentation(lines.get(adjustedLineNr)),
                        getCorrectFormat(diff.getExpected()),
                        parts2[1]
                );
                lines.add(adjustedLineNr, assertion);

                if (interactiveMode && !Interactor.promptSuggestion(testFile.toString(), lines, adjustedLineNr, comparison.getValue())) {
                    // Nothing to do
                } else {
                    insertionInformation.get(testFile.toString()).add(lineNr);
                    Files.write(testFile, lines);
                }

                // TODO: add `import static org.junit.Assert.*;` at top if necessary

                // Only consider one assertion per mutant, this is likely enough to kill the mutant.
                break;
            }
        }

    }

    /**
     * Get the indentation of a line.
     *
     * @param line The line.
     * @return The indentation.
     */
    private static String getIndentation(String line) {
        Matcher matcher = Pattern.compile("\\s+").matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return "            ";
    }

    /**
     * Get the correct format for a value.
     * Example: "Hello World!" => "\"Hello World!\""
     *
     * @param value The value to be formatted.
     * @return The formatted value.
     */
    private static String getCorrectFormat(Object value) {
        if (value instanceof String) {
            return String.format("\"%s\"", value);
        } else if (value instanceof Float) {
            return String.format("%sf", value);
        } else if (value instanceof Character) {
            return String.format("\'%s\'", value);
        }
        // TODO: else: ArrayReference or ObjectReference
        // else: no need to format

        return value.toString();
    }

}
