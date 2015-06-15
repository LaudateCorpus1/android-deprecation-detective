package de.fhg.aisec.deprecationDetective;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Michael Eder (michael.eder@aisec.fraunhofer.de)
 *
 */
public class XMLExporter {
	private Logger log;
	private File outputFile;
	DocumentBuilder xmlBuilder;
	Document deprecatedClasses;
	Element rootElement;
	
	@SuppressWarnings("unused")
	private XMLExporter() {	
	}
	
	
	public XMLExporter(File filename) {
		log = Logger.getLogger("DeprecationDetective");
		outputFile = filename;
		try {
			xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE, "Something with the XMLBuilder went wrong. Aborting");
			System.exit(1);
		}
		deprecatedClasses = xmlBuilder.newDocument();
		rootElement = deprecatedClasses.createElement("classes");
		deprecatedClasses.appendChild(rootElement);
	}
	/**
	 * Adds an entry for the given class and apiVersion in our XML file
	 * @param className
	 * @param apiVersion
	 */
	public void addEntry(Class<?> className, int apiVersion) {
		Element node = deprecatedClasses.createElement("deprecated");
		rootElement.appendChild(node);
		node.setAttribute("name", className.getName()); 
		node.setAttribute("api", String.valueOf(apiVersion));
	}
	
	/**
	 * Write everything to file
	 */
	public void write() {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(deprecatedClasses);
			StreamResult result = new StreamResult(outputFile);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			log.log(Level.SEVERE, "Something went wrong while writing the file");
		}
		
	}
}
