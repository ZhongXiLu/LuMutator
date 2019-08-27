package lumutator.parsers.pitest;

import lumutator.Mutant;
import lumutator.util.Directory;
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
public abstract class PITest {

    /**
     * Get all the survived mutants from the exported mutants of PITest.
     *
     * @param exportDirectory Directory that contains all the mutant files exported by PITest. (usually this is /target/pit-reports/export)
     * @return List of all the survived mutants.
     */
    static public List<Mutant> getMutants(String exportDirectory) throws IOException {
        List<Mutant> mutants = new ArrayList<>();

        // Retrieve all the survived mutants from the results file
        Set<Mutant> survivedMutants = null;
        for (File dir : new File(exportDirectory).listFiles()) {
            if (dir.isDirectory() && !dir.getName().equals("export")) {
                survivedMutants = getSurvivedMutants(Paths.get(dir.getCanonicalPath(), "mutations.xml").toFile());
            }
        }

        // Retrieve the necessary information of a mutant (including the corresponding class file)
        if (survivedMutants != null) {
            List<File> mutantDirectories = Directory.getAllDirectories(Paths.get(exportDirectory, "export").toFile(), "mutants");
            for (File dir : mutantDirectories) {
                for (File mutant : dir.listFiles()) {
                    BufferedReader reader = new BufferedReader(new FileReader(Paths.get(mutant.getCanonicalPath(), "details.txt").toString()));
                    String mutantDetails = reader.readLine();
                    //System.out.println(getAttribute(mutantDetails, "clazz"));
                }
            }
        } else {
            throw new IOException("Failed retrieving the survived mutants");
        }

        return mutants;
    }

    /**
     * Get all the survived mutants from the results file.
     *
     * @param resultsFile The results file from PITest.
     * @return Set of all the survived mutants.
     */
    static private Set<Mutant> getSurvivedMutants(File resultsFile) {
        Set<Mutant> survivedMutants = new HashSet<>();

        Document doc = null;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(resultsFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (doc != null) {
            doc.getDocumentElement().normalize();

            NodeList mutations = doc.getElementsByTagName("mutation");
            for (int i = 0; i < mutations.getLength(); i++) {
                Element mutation = (Element) mutations.item(i);
                if (!mutation.getAttribute("status").equals("KILLED")) {
                    // TODO: create Mutant object
                    //System.out.println(mutation.getElementsByTagName("sourceFile").item(0).getTextContent());
                    // TODO: ... parse other attributes
                }
            }
        }
        return survivedMutants;
    }

    /**
     * Get a certain attribute from the details.txt file for a mutant.
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

}
