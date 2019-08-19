package lumutator.purity;

import lumutator.Configuration;
import org.opalj.support.info.Purity;

/**
 * Purity analyzer with OPIUM (http://www.opal-project.de/Opium.html).
 */
public class PurityAnalyzer {

    /**
     * Run a purity analysis.
     *
     * @param config The configuration.
     */
    public PurityAnalyzer(Configuration config) {
        String[] arguments = new String[]{
                "-cp", config.get("classFiles"),    // path to directory with .class files
                "-individual",                      // print result of each method
                "-eval", "."                        // output directory for the results
        };
        Purity.main(arguments);
    }

}
