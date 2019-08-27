package lumutator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

/**
 * Small (singleton) class that serves as an interface for the configuration file.
 */
public final class Configuration {

    /**
     * The one instance of this class.
     */
    private static Configuration INSTANCE;

    /**
     * The configuration document.
     */
    private Document doc;

    /**
     * Get the one instance of this class.
     *
     * @return The one instance.
     */
    public static Configuration getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Configuration();
        }
        return INSTANCE;
    }

    /**
     * Constructor: parse a configuration file.
     *
     * @param filename Filename of the configuration file.
     * @throws IOException When something went wrong with parsing the configuration file.
     */
    public void initialize(String filename) throws IOException {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            this.doc = dBuilder.parse(new File(filename));
        } catch (Exception e) {
            throw new IOException("Something went wrong with parsing the configuration file: " + e.getMessage());
        }
        this.doc.getDocumentElement().normalize();
    }

    /**
     * Check if configuration has a specific parameter and their value set.
     *
     * @param key The key.
     * @return True if this configuration has this specific parameter and their value set.
     */
    public boolean hasParameter(String key) {
        NodeList nodes = this.doc.getElementsByTagName(key);
        return nodes.getLength() > 0 && !nodes.item(0).getTextContent().equals("");
    }

    /**
     * Get the value of a parameter in the configuration.
     *
     * @param key The key of the value.
     * @return The value of the key.
     */
    public String get(String key) throws IllegalArgumentException {
        if (this.hasParameter(key)) {
            return this.doc.getElementsByTagName(key).item(0).getTextContent();
        } else {
            throw new IllegalArgumentException(String.format("Could not find '%s' in the configuration or its value is not set", key));
        }
    }

    /**
     * Update the value of a parameter or in case the parameter doesn't exist already, create a new one.
     *
     * @param key   Name of the parameter.
     * @param value Value of the parameter.
     */
    public void set(String key, String value) {
        NodeList nodes = this.doc.getElementsByTagName(key);
        if (nodes.getLength() > 0) {
            // update value
            nodes.item(0).setTextContent(value);
        } else {
            // add new node with value
            Node newNode = this.doc.createElement(key);
            newNode.setTextContent(value);
            this.doc.getDocumentElement().appendChild(newNode);
        }
    }

}
