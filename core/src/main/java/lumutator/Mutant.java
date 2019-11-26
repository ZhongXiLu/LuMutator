package lumutator;

import lumutator.util.ANSIEscapeCodes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

/**
 * Simple class that stores information related to a mutant.
 */
public class Mutant {

    /**
     * The original, unmodified file where the mutant was inserted.
     */
    private File originalFile;

    /**
     * The compiled class file that contains the mutant.
     */
    private File classFile;

    /**
     * The mutated class, including its package. (e.g. some.package.Class)
     */
    private String mutatedClass;

    /**
     * The line number where the mutant resides.
     */
    private int lineNr;

    /**
     * The type of mutator.
     */
    private String mutator;

    /**
     * Extra information about the mutant. (can be anything, it's just to help the user in the end)
     */
    private String notes;

    /**
     * Did the mutant survive?
     */
    private Boolean survived = true;

    /**
     * Create a mutant.
     *
     * @param originalFile The original, unmodified file where the mutant was inserted.
     * @param classFile    The compiled class file that contains the mutant.
     * @param mutatedClass The mutated class, including its package.
     * @param lineNr       The line number where the mutant resides.
     * @param mutator      The type of mutator.
     * @param notes        Extra information about the mutant. (can be anything, it's just to help the user in the end)
     */
    public Mutant(File originalFile, File classFile, String mutatedClass, int lineNr, String mutator, String notes) {
        this.originalFile = originalFile;
        this.classFile = classFile;
        this.mutatedClass = mutatedClass;
        this.lineNr = lineNr;
        this.mutator = mutator;
        this.notes = notes;
    }

    /**
     * Get the original file.
     *
     * @return The original file.
     */
    public File getOriginalFile() {
        return originalFile;
    }

    /**
     * Get the compiled class file that contains the mutant.
     *
     * @return The class file.
     */
    public File getClassFile() {
        return classFile;
    }

    /**
     * Get the mutated class, including its package.
     *
     * @return The mutated class, including its package.
     */
    public String getMutatedClass() {
        return mutatedClass;
    }

    /**
     * Get the line number where the mutant resides.
     *
     * @return The line number.
     */
    public int getLineNr() {
        return lineNr;
    }

    /**
     * Get the type of mutator.
     *
     * @return The type of mutator.
     */
    public String getMutator() {
        return mutator;
    }

    /**
     * Get the extra information of the mutant.
     *
     * @return The extra information.
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Set whether the mutant survived or not.
     *
     * @param survived True if survived, false otherwise.
     */
    public void setSurvived(Boolean survived) {
        this.survived = survived;
    }

    /**
     * Check if the mutant survived.
     *
     * @return True if survived, false otherwise.
     */
    public Boolean survived() {
        return survived;
    }

    /**
     * Compare this mutant to another one.
     *
     * @param o The other mutant.
     * @return True if the mutant is located on the same location and is of the same mutator type.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mutant mutant = (Mutant) o;
        return lineNr == mutant.lineNr &&
                Objects.equals(originalFile, mutant.originalFile) &&
                Objects.equals(mutator, mutant.mutator);
    }

    /**
     * Create hashcode of this instance.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(originalFile, lineNr, mutator);
    }

    /**
     * Get string representation of the mutant.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        StringBuilder mutatedLine = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(originalFile.toPath());
            for (int l = lineNr - 4; l <= lineNr + 2; l++) {
                if (l >= 0 && l < lines.size()) {
                    if (l + 1 == lineNr) {
                        mutatedLine.append(ANSIEscapeCodes.ANSI_YELLOW + "! " + (l + 1) + lines.get(l) + ANSIEscapeCodes.ANSI_RESET + "\n");
                    } else {
                        mutatedLine.append("  " + (l + 1) + lines.get(l) + "\n");
                    }
                }
            }

            return String.format(
                    "Mutator: %s (%s)\n%s:\n%s",
                    mutator,
                    notes,
                    originalFile.getCanonicalPath(),
                    mutatedLine.toString()
            );

        } catch (IOException e) {
            return String.format(
                    "Mutator: %s (%s)",
                    mutator,
                    notes
            );
        }
    }
}
