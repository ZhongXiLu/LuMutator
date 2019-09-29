package lumutator.generator;

import lumutator.Mutant;
import lumutator.util.ANSIEscapeCodes;

import java.util.List;
import java.util.Scanner;

/**
 * Interactor: deals with user input as well as pretty-printing necessary information.
 */
public class Interactor {

    /**
     * Prompt an assertion suggestion to the user and ask if this should be added or not.
     *
     * @param file   Name of the file in which the new assertion should be added.
     * @param lines  Lines of the file; note that in this list, the new assertion should already be added.
     * @param lineNr The line number of the new assertion.
     * @param mutant The mutant the new assertion kills.
     * @return True if assertion should be added, otherwise false.
     */
    public static boolean promptSuggestion(String file, List<String> lines, int lineNr, Mutant mutant) {

        System.out.println("==============================");
        System.out.println("\n" + mutant.toString());

        System.out.println("\n" + file + ":");
        for (int l = lineNr - 3; l <= lineNr + 3; l++) {
            if (l >= 0 && l < lines.size()) {
                if (l == lineNr) {
                    System.out.println(ANSIEscapeCodes.ANSI_GREEN + l + lines.get(l) + ANSIEscapeCodes.ANSI_RESET);
                } else {
                    System.out.println(l + lines.get(l));
                }
            }
        }

        System.out.print("\nAdd this new assertion? (Y/N): ");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String response = scanner.nextLine().toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no")) {
                return false;
            } else {
                // Wrong response
                System.out.println("Please type \"Y\" or \"N\": ");
            }
        }

    }

}
