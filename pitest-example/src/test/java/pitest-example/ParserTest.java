package pitest;

import lumutator.Configuration;
import lumutator.Mutant;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests for {@link pitest.Parser}.
 */
public class ParserTest {

    /**
     * The survived mutants.
     */
    private Mutant m, m2;

    /**
     * Set up the default config.
     */
    @Before
    public void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            Configuration.getInstance().initialize(classLoader.getResource("default_config.xml").getFile());
        } catch (IOException e) {
            // Should not be possible
            fail();
        }

        m = new Mutant(
                new File("./src/main/java/some/package/Class.java"),
                new File(""),
                "some.package.Class",
                123,
                "VoidMethodCallMutator",
                "removed call to some/package/Class::someMethod"
        );
        m2 = new Mutant(
                new File("./src/main/java/some/package/OtherClass.java"),
                new File(""),
                "some.package.OtherClass",
                666,
                "ReturnValsMutator",
                "replaced return of integer sized value with (x == 0 ? 1 : 0)"
        );
    }

    /**
     * Test the {@link pitest.Parser#getSurvivedMutantsFromFile(File)} method.
     */
    @Test
    public void testGetSurvivedMutantsFromFile() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            Method method = pitest.Parser.class.getDeclaredMethod("getSurvivedMutantsFromFile", File.class);
            method.setAccessible(true);
            Set<Mutant> survivedMutants = (Set<Mutant>) method.invoke(null, new File(classLoader.getResource("pit-reports/201908271440/mutations.xml").getFile()));

            assertEquals(2, survivedMutants.size());

            assertTrue(survivedMutants.contains(m));
            assertTrue(survivedMutants.contains(m2));

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link pitest.Parser#getMutants(String, boolean)} method for survived mutants.
     */
    @Test
    public void testGetSurvivedMutants() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            Method method = pitest.Parser.class.getDeclaredMethod("getMutants", String.class, boolean.class);
            method.setAccessible(true);
            List<Mutant> survivedMutants = (List<Mutant>) method.invoke(null, classLoader.getResource("pit-reports").getPath(), true);

            assertEquals(2, survivedMutants.size());

            assertTrue(survivedMutants.contains(m));
            assertTrue(survivedMutants.contains(m2));

            // Also check if the .class files are set
            assertNotSame("", survivedMutants.get(0).getClassFile());
            assertNotSame("", survivedMutants.get(1).getClassFile());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            fail();
        }
    }

    /**
     * Test the {@link pitest.Parser#getMutants(String, boolean)} method for all the mutants.
     */
    @Test
    public void testGetAllMutants() {
        ClassLoader classLoader = getClass().getClassLoader();

        try {
            Method method = pitest.Parser.class.getDeclaredMethod("getMutants", String.class, boolean.class);
            method.setAccessible(true);
            List<Mutant> mutants = (List<Mutant>) method.invoke(null, classLoader.getResource("pit-reports").getPath(), false);

            assertEquals(4, mutants.size());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            // Should not be possible
            System.out.println(e);
            fail();
        }
    }

}