package lumutator.parsers.pitest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * Parser for the generated mutations xml file by PITest.
 */
public class PITest {

    /**
     * Constructor.
     *
     * @param filename Filename of the generated mutations file.
     */
    public PITest(String filename) {

        Document doc = null;
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = dBuilder.parse(new File(filename));
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

    }

}
