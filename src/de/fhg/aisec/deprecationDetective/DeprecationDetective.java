package de.fhg.aisec.deprecationDetective;

import java.io.File;
import java.io.FileNotFoundException;
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
		XMLExporter out = new XMLExporter(new File(output));
		
		try {
			log.log(Level.INFO, "Parsing sdkLocation");
			parser = new SDKParser(new File(sdkLocation));
			log.log(Level.INFO, "Done");
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Directory with platform SDKs does not exist! Aborting.");
			System.exit(1);
		}
		for(int sdkVersion : parser.getSDKVersions()) {
			log.log(Level.INFO, "Searching for deprecated classes in API level " + sdkVersion);
			for (Class<?> className : Analyzer.getDeprecatedClasses(parser.getPath(sdkVersion))) {
				out.addEntry(className, sdkVersion);
			}
			// Build a list of non-deprecated classes basing on the latest SDK. This is useful
			// in order to differentiate between SDK classes and classes from the app
			if(sdkVersion == parser.getMaxSDK()) {
				for(Class<?> className : Analyzer.getNonDeprecatedClasses(parser.getPath(sdkVersion))) {
					out.addEntry(className, -1);
				}
			}
		}
		log.log(Level.INFO, "Wrtiting to file " + output);
		out.write();
		log.log(Level.INFO, "Finished!");
	}
}
