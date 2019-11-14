package lumutator;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for {@link Configuration}.
 */
public class ConfigurationTest {

    /**
     * The configuration used in the tests.
     */
    private Configuration config;

    @Before
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Configuration.getInstance().initialize(classLoader.getResource("bad_config.xml").getFile());
            config = Configuration.getInstance();
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Test the {@link Configuration#hasParameter(String)} method.
     */
    @Test
    public void testHasParameter() {
        assertTrue(config.hasParameter("projectDir"));
        assertFalse(config.hasParameter("testDir"));    // node exists, but value is missing
        assertFalse(config.hasParameter("compileCommand"));
    }

    /**
     * Test the {@link Configuration#get(String)} method.
     */
    @Test
    public void testGet() {
        assertEquals("some_project", config.get("projectDir"));
        boolean exceptionThrown = false;
        try {
            config.get("testDir");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            config.get("compileCommand");
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    /**
     * Test the {@link Configuration#set(String, String)} method.
     */
    @Test
    public void testSet() {
        assertEquals("some_project", config.get("projectDir"));
        config.set("projectDir", "some_other_project");
        assertEquals("some_other_project", config.get("projectDir"));

        assertFalse(config.hasParameter("newParameter"));
        config.set("newParameter", "420");
        assertEquals("420", config.get("newParameter"));
    }

}
