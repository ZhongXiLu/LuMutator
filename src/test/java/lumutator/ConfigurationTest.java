package lumutator;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for Configuration.
 */
public class ConfigurationTest {

    /**
     * Test the hasParameter method.
     */
    @Test
    public void testHasParameter() {
        ClassLoader classLoader = getClass().getClassLoader();
        Configuration config = null;
        try {
            config = new Configuration(classLoader.getResource("config.xml").getFile());
        } catch (IOException e) {
            fail();
        }

        assertTrue(config.hasParameter("projectDir"));
        assertTrue(config.hasParameter("testDir"));
        assertFalse(config.hasParameter("compileCommand"));
    }

    /**
     * Test the get method.
     */
    @Test
    public void testGet() {
        ClassLoader classLoader = getClass().getClassLoader();
        Configuration config = null;
        try {
            config = new Configuration(classLoader.getResource("config.xml").getFile());
        } catch (IOException e) {
            fail();
        }

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

}
