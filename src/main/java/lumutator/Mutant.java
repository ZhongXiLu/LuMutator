package lumutator;

import java.io.File;

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
     * The line number where the mutant resides.
     */
    private int lineNr;

    /**
     * The type of mutator.
     * TODO: better to create enum?
     */
    private String mutator;

    /**
     * Extra information about the mutant. (can be anything, it's just to help the user in the end)
     */
    private String notes;

    // TODO: store other properties

    /**
     * Create a mutant.
     *
     * @param originalFile The original, unmodified file where the mutant was inserted.
     * @param classFile    The compiled class file that contains the mutant.
     * @param lineNr       The line number where the mutant resides.
     * @param mutator      The type of mutator.
     * @param notes        Extra information about the mutant.
     */
    public Mutant(File originalFile, File classFile, int lineNr, String mutator, String notes) {
        this.originalFile = originalFile;
        this.classFile = classFile;
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
}
