package pitest;

import lumutator.Configuration;
import lumutator.Mutant;
import lumutator.util.Directory;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the exported mutants by PITest.
 */
public abstract class Parser {

    /**
     * Get all the survived mutants from the exported mutants of PITest.
     *
     * @param exportDirectory Directory that contains all the mutant files exported by PITest. (usually this is /target/pit-reports)
     * @param survivedOnly    Only get the survived mutants?
     * @return List of all the survived mutants.
     */
    static public List<Mutant> getMutants(String exportDirectory, boolean survivedOnly) throws IOException {
        // SMALL NOTE:
        // PITest generates two directories:
        //      - Directory with `mutations.xml` which contains the result of the mutation process,
        //        i.e. whether a mutant is killed or not and some additional information
        //      - Directory with all the generated mutants WITHOUT their results (killed or not),
        //        importantly, this directory also contains the compiled class files with the mutant inside of them
        //
        // So to get all the survived mutants:
        //      (1) Get the results of the mutants from `mutations.xml`
        //      (2) Iterate over the generated mutants and see if they're killed or not according to `mutations.xml`
        //      (3) Based on the last two steps, we can store all the necessary information of a mutant

        File exportDir = new File(exportDirectory);
        if (!exportDir.exists()) {
            throw new IOException("Cannot find PITest reports; make sure '" + exportDirectory + "' is present");
        }

        // Use list, so we can preserve order of insertion,
        // this way, the mutants are grouped together based on class
        List<Mutant> mutants = new ArrayList<>();

        // Retrieve all the survived mutants from the results file
        Set<Mutant> survivedMutantsFromResults = null;
        if (survivedOnly) {
            for (File dir : exportDir.listFiles()) {
                if (dir.isDirectory() && !dir.getName().equals("export")) {
                    survivedMutantsFromResults = getSurvivedMutantsFromFile(Paths.get(dir.getCanonicalPath(), "mutations.xml").toFile());
                }
            }
        }

        // Retrieve the necessary information of a mutant (including the corresponding class file)
        if (survivedMutantsFromResults != null || !survivedOnly) {
            List<File> mutantDirectories = Directory.getAllDirectories(Paths.get(exportDirectory, "export").toFile(), "mutants");
            for (File dir : mutantDirectories) {
                for (File mutant : dir.listFiles()) {
                    BufferedReader reader = new BufferedReader(new FileReader(Paths.get(mutant.getCanonicalPath(), "details.txt").toString()));
                    String mutantDetails = reader.readLine();

                    // Get details from "details.txt"
                    String mutatedClass = getAttribute(mutantDetails, "clazz");
                    String originalFile = Configuration.getInstance().get("projectDir") + "/" + getOriginalFilePath(mutatedClass);
                    int lineNr = Integer.parseInt(getAttribute(mutantDetails, "lineNumber"));
                    String mutator = getAttribute(mutantDetails, "mutator");
                    Matcher matcher = Pattern.compile("mutators\\.([^.]+)\\b").matcher(mutator);
                    if (matcher.find()) {
                        // Only get the mutator name (not the complete package it is included in)
                        mutator = matcher.group(1);
                    }
                    String notes = getAttribute(mutantDetails, "description");

                    // Get the .class file in the same directory
                    File classFile = ((List<File>) FileUtils.listFiles(mutant, new String[]{"class"}, true)).get(0);

                    Mutant m = new Mutant(
                            new File(originalFile),
                            classFile,
                            mutatedClass,
                            lineNr,
                            mutator,
                            notes
                    );
                    if (!survivedOnly || survivedMutantsFromResults.contains(m)) {
                        mutants.add(m);
                    }
                }
            }
        } else {
            throw new IOException("Failed retrieving the survived mutants");
        }

        return mutants;
    }

    /**
     * Get all the survived mutants from the results file (`mutations.xml`).
     *
     * @param resultsFile The results file from PITest.
     * @return Set of all the survived mutants.
     */
    static private Set<Mutant> getSurvivedMutantsFromFile(File resultsFile) throws IOException {
        Set<Mutant> survivedMutants = new HashSet<>();

        Document doc = null;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(resultsFile);
        } catch (Exception e) {
            throw new IOException("Failed parsing the mutations.xml file: " + e.getMessage());
        }

        if (doc != null) {
            doc.getDocumentElement().normalize();

            NodeList mutations = doc.getElementsByTagName("mutation");
            for (int i = 0; i < mutations.getLength(); i++) {
                Element mutation = (Element) mutations.item(i);
                if (!mutation.getAttribute("status").equals("KILLED")) {

                    // ORIGINAL FILE
                    // Path to original file isn't stored, so deduct manually
                    String mutatedClass = mutation.getElementsByTagName("mutatedClass").item(0).getTextContent();
                    String originalFile = Configuration.getInstance().get("projectDir") + "/" + getOriginalFilePath(mutatedClass);

                    // MUTATOR
                    String mutator = mutation.getElementsByTagName("mutator").item(0).getTextContent();
                    Matcher matcher = Pattern.compile("mutators\\.([^.]+)\\b").matcher(mutator);
                    if (matcher.find()) {
                        // Only get the mutator name (not the complete package it is included in)
                        mutator = matcher.group(1);
                    }

                    // LINENR
                    int lineNr = Integer.parseInt(mutation.getElementsByTagName("lineNumber").item(0).getTextContent());

                    // NOTES
                    String notes = mutation.getElementsByTagName("description").item(0).getTextContent();

                    survivedMutants.add(new Mutant(
                            new File(originalFile),
                            new File(""),   // not necessary now
                            mutatedClass,
                            lineNr,
                            mutator,
                            notes
                    ));
                }
            }
        }

        return survivedMutants;
    }

    /**
     * Get a certain attribute from the `details.txt` file for a mutant.
     *
     * @param data      The line in the file.
     * @param attribute The wanted attribute.
     * @return The value of the attribute.
     */
    static private String getAttribute(String data, String attribute) {
        Matcher matcher = Pattern.compile(String.format("\\b%s=([^,]+),", attribute)).matcher(data);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Get the complete path to a source file.
     *
     * @param file The source file.
     * @return The complete path to the source file.
     */
    static private String getOriginalFilePath(String file) {
        String sourcePath = Configuration.getInstance().get("sourcePath");
        return String.format("%s/%s.java", sourcePath, Directory.packagePathToFilePath(file));
    }

}
