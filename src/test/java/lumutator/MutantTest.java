package lumutator;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Tests for {@link Mutant}.
 */
public class MutantTest {

    /**
     * A simple mutant.
     */
    private Mutant m;

    /**
     * Another mutant.
     */
    private Mutant m2;

    /**
     * Set up a few mutants.
     */
    @Before
    public void setUp() {
        m = new Mutant(
                new File("originalFile.java"),
                new File("originalFile.class"),
                123,
                "MathMutator",
                "Some notes."
        );
        m2 = new Mutant(
                new File(""),
                new File(""),
                -1,
                "",
                "."
        );
    }

    /**
     * Test the constructor and all the getters.
     */
    @Test
    public void testGetters() {
        assertEquals("originalFile.java", m.getOriginalFile().getName());
        assertEquals("originalFile.class", m.getClassFile().getName());
        assertEquals(123, m.getLineNr());
        assertEquals("MathMutator", m.getMutator());
        assertEquals("Some notes.", m.getNotes());

        assertEquals("", m2.getOriginalFile().getName());
        assertEquals("", m2.getClassFile().getName());
        assertEquals(-1, m2.getLineNr());
        assertEquals("", m2.getMutator());
        assertEquals(".", m2.getNotes());
    }

    /**
     * Test the {@link Mutant#equals(Object)} method.
     */
    @Test
    public void testEquals() {
        assertFalse(m.equals(m2));
        assertFalse(m2.equals(m));

        Mutant sameMutantAsM = new Mutant(
                new File("originalFile.java"),
                new File("originalFile.class"),
                123,
                "MathMutator",
                "Some notes."
        );
        assertTrue(m.equals(sameMutantAsM));
        assertTrue(sameMutantAsM.equals(m));
    }

    /**
     * Test the {@link Mutant#hashCode()} method.
     */
    @Test
    public void testHashCode() {
        assertEquals(-213787984, m.hashCode());
        assertEquals(1186212241, m2.hashCode());
    }

}