package lumutator.debugger;

import lumutator.Configuration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Debugger.
 */
public class DebuggerTest {

    private Debugger debugger;

    @Before
    public void setUp() {
        Configuration config;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            config = new Configuration(classLoader.getResource("config_debugger.xml").getFile());
            config.set("classPath", System.getProperty("java.class.path"));
            debugger = new Debugger(config, "lumutator.ConfigurationTest");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testCommands() {
        debugger.run();
    }
}
