package lumutator.parsers.pitest;

import lumutator.util.Directory;

import java.io.File;
import java.util.List;

/**
 * Parser for the exported mutants by PITest.
 */
public class PITest {

    /**
     * Constructor.
     *
     * @param directory Directory that contains all the mutant files exported by PITest. (usually this is /target/pit-reports/export)
     */
    public PITest(String directory) {

        List<File> mutantDirectories = Directory.getAllDirectories(new File(directory), "mutants");
        System.out.println(mutantDirectories);

    }

}
