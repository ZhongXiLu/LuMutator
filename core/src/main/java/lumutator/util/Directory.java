package lumutator.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * Translate the package path to a file path.
     * Example: some.package.Class$1 => some/package/Class
     *
     * @param packagePath The package path.
     * @return The file path.
     */
    public static String packagePathToFilePath(String packagePath) {
        String filePath = packagePath;

        // Strip $ and following characters
        if (filePath.contains("$")) {
            Matcher matcher = Pattern.compile("([^$]+)\\$").matcher(filePath);
            if (matcher.find()) {
                filePath = matcher.group(1);
            }
        }
        filePath = filePath.replace('.', '/');

        return filePath;
    }

}
