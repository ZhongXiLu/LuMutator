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
     * @param file     Name of the file in which the new assertion should be added.
     * @param lines    Lines of the file; note that in this list, the new assertion should already be added.
     * @param lineNr   The line number of the new assertion.
     * @param mutant   The mutant the new assertion kills.
     * @param current  The current assertion number.
     * @param total    The total amount of assertions.
     * @return True if assertion should be added, otherwise false.
     */
    public static boolean promptSuggestion(String file, List<String> lines, int lineNr, Mutant mutant, int current, int total) {

        System.out.println("=======================================");
        System.out.println("\n" + mutant.toString());

        System.out.println("\n" + file + ":");
        for (int l = lineNr - 3; l <= lineNr + 3; l++) {
            if (l >= 0 && l < lines.size()) {
                if (l == lineNr) {
                    System.out.println(ANSIEscapeCodes.ANSI_GREEN + "+ " + (l+1) + lines.get(l) + ANSIEscapeCodes.ANSI_RESET);
                } else {
                    System.out.println("  " + (l+1) + lines.get(l));
                }
            }
        }

        System.out.print(String.format("\n(%s/%s) Add this new assertion? (Y/N): ", current, total));
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
