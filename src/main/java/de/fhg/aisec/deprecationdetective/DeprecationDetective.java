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
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Michael Eder (michael.eder@aisec.fraunhofer.de)
 *
 */
public class DeprecationDetective {
	private static final Logger log = Logger.getLogger("DeprecationDetective");
	static String output = "deprecatedResources.xml";
	static File sdkLocation = new File("/opt/android-sdk/platforms");
	
	/**
	 * Gets everything done:
	 * - Parse the sdkLocation for available SDKs
	 * - Get all classes of each SDK and find the @Deprecated ones
	 * - Write everything into the XML file determined by the output variable
	 */
	public static void main(String[] args) {
		// Parse args
		List<String> argsl = Arrays.asList(args);
		if (argsl.contains("-s") && argsl.indexOf("-s")+1<argsl.size()) {
			sdkLocation = new File(argsl.get(argsl.indexOf("-s")+1));
			if (!sdkLocation.exists() || !sdkLocation.canRead()) {
				printUsage();
				System.exit(-1);
			}
		} else if (argsl.contains("-o") && argsl.indexOf("-o")+1<argsl.size()) {
			output = argsl.get(argsl.indexOf("-o")+1);
		} else if (argsl.size()>0) {
			printUsage();
			System.exit(-1);
		}
		
		SDKParser parser = null;
		XMLExporter xmlOut = new XMLExporter(new File(output));
		try {
			log.log(Level.INFO, "Parsing sdkLocation");
			parser = new SDKParser(sdkLocation);
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

	private static void printUsage() {
		System.out.println("Android Deprecation Detective");
		System.out.println("Usage: java -jar android-deprecation-detective <options>");
		System.out.println("Options: ");
		System.out.println("-h                      Show this help");
		System.out.println("-s <android-sdk-dir>    Path to Android SDK platforms. Default: /opt/android-sdk/platforms");
		System.out.println("-o <output file>        Name of output file. Default: deprecatedResources.xml");
		
	}
}
