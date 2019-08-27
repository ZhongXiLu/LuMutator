package lumutator.parsers.pitest;

import lumutator.Configuration;
import lumutator.Mutant;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for {@link PITest}.
 */
public class PITestTest {


    /**
     * Set up the default config.
     */
    @Before
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Configuration.getInstance().initialize(classLoader.getResource("default_config.xml").getFile());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the {@link PITest#getSurvivedMutantsFromFile(File)} method.
     */
    @Test
    public void testGetSurvivedMutantsFromFile() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            Method method = PITest.class.getDeclaredMethod("getSurvivedMutantsFromFile", File.class);
            method.setAccessible(true);
            Set<Mutant> survivedMutants = (Set<Mutant>) method.invoke(null, new File(classLoader.getResource("pit-reports/201908271440/mutations.xml").getFile()));

            assertEquals(2, survivedMutants.size());

            Mutant m = new Mutant(
                    new File("src/main/java/some/package/Class.java"),
                    new File(""),
                    123,
                    "VoidMethodCallMutator",
                    "removed call to some/package/Class::someMethod"
            );
            Mutant m2 = new Mutant(
                    new File("src/main/java/some/package/OtherClass.java"),
                    new File(""),
                    666,
                    "ReturnValsMutator",
                    "replaced return of integer sized value with (x == 0 ? 1 : 0)"
            );

            assertTrue(survivedMutants.contains(m));
            assertTrue(survivedMutants.contains(m2));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Test the {@link PITest#getSurvivedMutants(String)} method.
     */
    @Test
    public void testGetSurvivedMutants() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            Method method = PITest.class.getDeclaredMethod("getSurvivedMutants", String.class);
            method.setAccessible(true);
            List<Mutant> survivedMutants = (List<Mutant>) method.invoke(null, classLoader.getResource("pit-reports").getPath());

            assertEquals(2, survivedMutants.size());

            Mutant m = new Mutant(
                    new File("src/main/java/some/package/Class.java"),
                    new File(""),
                    123,
                    "VoidMethodCallMutator",
                    "removed call to some/package/Class::someMethod"
            );
            Mutant m2 = new Mutant(
                    new File("src/main/java/some/package/OtherClass.java"),
                    new File(""),
                    666,
                    "ReturnValsMutator",
                    "replaced return of integer sized value with (x == 0 ? 1 : 0)"
            );

            assertTrue(survivedMutants.contains(m));
            assertTrue(survivedMutants.contains(m2));

            // Also check if the .class files are set
            assertNotSame("", survivedMutants.get(0).getClassFile());
            assertNotSame("", survivedMutants.get(1).getClassFile());

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}