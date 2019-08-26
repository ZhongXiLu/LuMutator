package lumutator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Some utilities function related to directories.
 */
public class Directory {

    /**
     * Get all the directories with a specific name in a directory.
     *
     * @param searchDirectory The directory to search in.
     * @return List of all the found directories.
     */
    public static List<File> getAllDirectories(File searchDirectory, String name) {
        List<File> directories = new ArrayList<>();

        for (File file : searchDirectory.listFiles()) {
            if (file.isDirectory()) {
                if (file.getName().equals(name)) {
                    directories.add(file);
                }
                directories.addAll(getAllDirectories(file, name));
            }
        }

        return directories;
    }

}
