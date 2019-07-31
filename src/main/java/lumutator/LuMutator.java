package lumutator;

import lumutator.parsers.pitest.PITest;

public class LuMutator {

    public static void main(String argv[]) {
        if (argv.length > 0) {
            PITest piTest = new PITest(argv[0]);
        }
    }
}
