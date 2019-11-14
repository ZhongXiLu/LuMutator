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

    /**
     * Test the {@link Directory#packagePathToFilePath(String)} method.
     */
    @Test
    public void testPackagePathToFilePath() {
        assertEquals("", Directory.packagePathToFilePath(""));
        assertEquals("Class", Directory.packagePathToFilePath("Class"));
        assertEquals("Class", Directory.packagePathToFilePath("Class$1"));
        assertEquals("some/package/Class", Directory.packagePathToFilePath("some.package.Class"));
        assertEquals("some/package/Class", Directory.packagePathToFilePath("some.package.Class$1$2"));
    }
}
