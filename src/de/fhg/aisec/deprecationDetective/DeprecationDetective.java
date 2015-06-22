package de.fhg.aisec.deprecationDetective;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Michael Eder (michael.eder@aisec.fraunhofer.de)
 *
 */
public class DeprecationDetective {
	private static String cwd = Paths.get("").toAbsolutePath().toString(); // the directory this class is invoked from
	//////////////// CHANGE ME /////////////////////////////////
	static String output = cwd + "/deprecatedClasses.xml";
	static String sdkLocation = "/opt/android-sdk/platforms/";
	
	/**
	 * Gets everything done:
	 * - Parse the sdkLocation for available SDKs
	 * - Get all classes of each SDK and find the @Deprecated ones
	 * - Write everything into the XML file determined by the output variable
	 */
	public static void main(String[] args) {
		Logger log = Logger.getLogger("DeprecationDetective");
		SDKParser parser = null;
		XMLExporter xmlOut = new XMLExporter(new File(output));
		try {
			log.log(Level.INFO, "Parsing sdkLocation");
			parser = new SDKParser(new File(sdkLocation));
			log.log(Level.INFO, "Done");
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Directory with platform SDKs does not exist! Aborting.");
			System.exit(1);
		}
		for(int sdkVersion : parser.getSDKVersions()) {
			log.log(Level.INFO, "Searching for deprecated items in API level " + sdkVersion);
			Analyzer currentSDKAnalyzer = new Analyzer(parser.getPath(sdkVersion));
			
			// Get all deprecated classes and methods
			for (Class<?> className : currentSDKAnalyzer.getDeprecatedClasses()) {
				xmlOut.addEntryForDeprecatedClass(className, sdkVersion);
			}
			for(Method method : currentSDKAnalyzer.getDeprecatedMethods()) {
				xmlOut.addEntryForDeprecatedMethod(method, sdkVersion);
			}
			
			// Build a list of non-deprecated classes and methods basing on the latest SDK. This is useful
			// in order to differentiate between SDK classes/methods and classes/methods from the app
			if(sdkVersion == parser.getMaxSDK()) {
				log.log(Level.INFO, "Searching for non-deprecated items in latest available API level " + sdkVersion);
				for(Class<?> className : currentSDKAnalyzer.getNonDeprecatedClasses()) {
					xmlOut.addEntryForNonDeprecatedClass(className);
				}
				for(Method method : currentSDKAnalyzer.getDeprecatedMethods()) {
					xmlOut.addEntryForNonDeprecatedMethod(method);
				}
			}
			
			currentSDKAnalyzer.cleanUp(); // Important! Deletes the files extracted from the Android SDK
		}
		log.log(Level.INFO, "Wrtiting to file " + output);
		xmlOut.write();
		log.log(Level.INFO, "Finished!");
	}
}
