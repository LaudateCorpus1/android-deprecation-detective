/**
The MIT License (MIT)

Copyright (c) 2015, Fraunhofer AISEC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.fhg.aisec.deprecationdetective;

import java.io.File;
import java.lang.reflect.Executable;
import java.util.Arrays;
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
	 * @param relation
	 * @param apiVersion
	 */
	public void addEntryForDeprecatedMethod(ClassMethodTuple relation, int apiVersion) {
		Executable method = relation.getAccordingMethod();
		Class<?> classObject = relation.getClassMethodIsAvailableIn(); 
		Element node = xmlDoc.createElement("deprecated");
		methods.appendChild(node);
		node.setAttribute("name", method.getName());
		node.setAttribute("class", classObject.getName());
		node.setAttribute("paramTypes", String.join(" | ", Arrays.asList(method.getParameterTypes()).stream().map(e -> e.getName()).toArray(String[]::new)));
		node.setAttribute("api", String.valueOf(apiVersion));
	}
	
	/**
	 * Adds an entry for the given non-deprecated method.
	 * @param relation
	 * @param apiVersion
	 */
	public void addEntryForNonDeprecatedMethod(ClassMethodTuple relation) {
		Executable method = relation.getAccordingMethod();
		Class<?> classObject = relation.getClassMethodIsAvailableIn();
		Element node = xmlDoc.createElement("non-deprecated");
		methods.appendChild(node);
		node.setAttribute("name", method.getName());
		node.setAttribute("class", classObject.getName());
		node.setAttribute("paramTypes", String.join(" | ", Arrays.asList(method.getParameterTypes()).stream().map(e -> e.getName()).toArray(String[]::new)));
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
}
