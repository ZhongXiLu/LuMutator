package lumutator;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Basic environment (bank application) for some tests.
 */
public class TestEnvironment {

    /**
     * All the inspector methods from the bank application.
     */
    protected static final Set<String> inspectorMethods = new HashSet<>(Arrays.asList(
            "bank.Customer.getName",
            "bank.Customer.getAccountNumber",
            "bank.Customer.getBalance",
            "bank.Bank.getLastAddedCustomer"
    ));

    /**
     * Some initial configuration and setting the correct environment,
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        ClassLoader classLoader = TestEnvironment.class.getClassLoader();
        try {
            // Set up
            Configuration.getInstance().initialize(classLoader.getResource("bank_config.xml").getFile());
            Configuration config = Configuration.getInstance();

            // Small hack to get junit jar added to classpath
            String[] classpathEntries = System.getProperty("java.class.path").split(File.pathSeparator);
            for (String classpathEntry : classpathEntries) {
                if (classpathEntry.contains("junit")) {
                    config.set("classPath", config.get("classPath") + ":" + classpathEntry);
                }
            }

            Process process = Runtime.getRuntime().exec(config.get("testCommand"), null, new File(config.get("projectDir")));
            process.waitFor();
        } catch (Exception e) {
            e.getStackTrace();
            fail();
        }
    }

    /**
     * Clean up the working directory (bank application).
     */
    @AfterClass
    public static void tearDownAfterClass() {
        try {
            // Clean up
            Process process2 = Runtime.getRuntime().exec("mvn clean", null, new File(Configuration.getInstance().get("projectDir")));
            process2.waitFor();

        } catch (Exception e) {
            e.getStackTrace();
            fail();
        }
    }
}
