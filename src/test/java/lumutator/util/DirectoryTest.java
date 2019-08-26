package lumutator.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link Directory}.
 */
public class DirectoryTest {

    /**
     * Test the {@link Directory#getAllDirectories(File, String)} method.
     */
    @Test
    public void testGetAllDirectories() {

        assertEquals(1, Directory.getAllDirectories(new File("src/main"), "util").size());
        assertEquals(0, Directory.getAllDirectories(new File("src"), "non_existing_dir").size());
        assertEquals(2, Directory.getAllDirectories(new File("src"), "lumutator").size());
    }
}
