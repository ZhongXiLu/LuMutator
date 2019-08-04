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

    /**
     * Test the isRunning method.
     */
    @Test
    public void testIsRunning() {
        assertFalse(debugger.isRunning());
        Thread thread = new Thread(debugger);
        thread.start();
        try {
            Thread.sleep(1000); // 1 second should be enough?
        } catch (InterruptedException e) {
        }
        assertTrue(debugger.isRunning());
    }
}
