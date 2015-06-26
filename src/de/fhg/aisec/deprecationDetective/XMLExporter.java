package de.fhg.aisec.deprecationDetective;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
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
	private DocumentBuilder xmlBuilder;
	private Document xmlDoc;
	private Element rootElement;
	private Element classes;
	private Element methods;
	
	@SuppressWarnings("unused")
	private XMLExporter() {	
	}
	
	/**
	 * Initialize the XMLExporter and create the basic structure of the XML document
	 * @param filename
	 */
	public XMLExporter(File filename) {
		log = Logger.getLogger("DeprecationDetective");
		outputFile = filename;
		try {
			xmlBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.log(Level.SEVERE, "Something with the XMLBuilder went wrong. Aborting");
			System.exit(1);
		}
		xmlDoc = xmlBuilder.newDocument();
		rootElement = xmlDoc.createElement("data");
		xmlDoc.appendChild(rootElement);
		classes = xmlDoc.createElement("classes");
		methods = xmlDoc.createElement("methods");
		rootElement.appendChild(classes);
		rootElement.appendChild(methods);
	}
	
	/**
	 * Adds an entry for the given deprecated class and apiVersion in our XML file
	 * @param className
	 * @param apiVersion
	 */
	public void addEntryForDeprecatedClass(Class<?> className, int apiVersion) {
		Element node = xmlDoc.createElement("deprecated");
		classes.appendChild(node);
		node.setAttribute("name", className.getName()); 
		node.setAttribute("api", String.valueOf(apiVersion));
	}
	
	/**
	 * Adds an entry for the given non-deprecated class.
	 * @param className
	 * @param apiVersion
	 */
	public void addEntryForNonDeprecatedClass(Class<?> className) {
		Element node = xmlDoc.createElement("non-deprecated");
		classes.appendChild(node);
		node.setAttribute("name", className.getName()); 
	}
	
	/**
	 * Adds and entry for the given deprecated method and apiVersion in our XML file
	 * @param m
	 * @param apiVersion
	 */
	public void addEntryForDeprecatedMethod(Method m, int apiVersion) {
		Element node = xmlDoc.createElement("deprecated");
		methods.appendChild(node);
		node.setAttribute("name", m.getName());
		node.setAttribute("class", m.getDeclaringClass().getName());
		node.setAttribute("paramTypes", join(Arrays.asList(m.getParameterTypes()).iterator(), " | "));
		node.setAttribute("api", String.valueOf(apiVersion));
	}
	
	/**
	 * Adds an entry for the given non-deprecated method.
	 * @param m
	 * @param apiVersion
	 */
	public void addEntryForNonDeprecatedMethod(Method m) {
		Element node = xmlDoc.createElement("non-deprecated");
		methods.appendChild(node);
		node.setAttribute("name", m.getName());
		node.setAttribute("class", m.getDeclaringClass().getName());
		node.setAttribute("paramTypes", join(Arrays.asList(m.getParameterTypes()).iterator(), " | "));
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
			DOMSource source = new DOMSource(xmlDoc);
			StreamResult result = new StreamResult(outputFile);
			transformer.transform(source, result);
		} catch (TransformerException e) {
			log.log(Level.SEVERE, "Something went wrong while writing the file");
		}
	}
	
	/**
	 * Join an Iterator with a separator String 
	 * Taken from apache.commons.lang3.StringUtils and modified to print beautiful class names
	 * http://commons.apache.org/proper/commons-lang/javadocs/api-release/org/apache/commons/lang3/StringUtils.html
	 */
	private static String join(final Iterator<Class<?>> iterator, final String separator) {
		// handle null, zero and one elements before building a buffer
		if (iterator == null) {
			return null;
		}
		if (!iterator.hasNext()) {
			return "";
		}
		final Object first = iterator.next();
		if (!iterator.hasNext()) {
			final String result = ((Class<?>)first).getName();
			return result;
		}

		// two or more elements
		final StringBuilder buf = new StringBuilder(256); // Java default is 16,
															// probably too
															// small
		if (first != null) {
			buf.append(((Class<?>)first).getName());
		}

		while (iterator.hasNext()) {
			if (separator != null) {
				buf.append(separator);
			}
			final Object obj = iterator.next();
			if (obj != null) {
				buf.append(((Class<?>)obj).getName());
			}
		}
		return buf.toString();
	}
}
