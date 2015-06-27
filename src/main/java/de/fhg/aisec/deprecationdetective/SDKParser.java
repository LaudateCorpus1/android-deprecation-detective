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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Michael Eder (michael.eder@aisec.fraunhofer.de)
 *
 */
public class SDKParser {
	Logger log;
	private File platformPath;
	private int maxSDK;
	private ArrayList<Integer> availableSDKs;
	
	@SuppressWarnings("unused")
	private SDKParser() {
		// Nothing to see here
	}
	
	/**
	 * Initialize the object with the directory containing the Android platform SDKs,
	 * for example /opt/android-sdk/platforms/
	 * @param platformDirectory
	 * @throws FileNotFoundException 
	 */
	public SDKParser(File platformDirectory) throws FileNotFoundException {
		log = Logger.getLogger("DeprecationDetective");
		if(!platformDirectory.getAbsoluteFile().exists()) {
			log.log(Level.SEVERE, "given SDK path does not exist");
			throw new FileNotFoundException();
		}
		platformPath = platformDirectory;
		availableSDKs = new ArrayList<Integer>();
		maxSDK = 0;
		analyze();
	}
	
	/**
	 * @return The highest SDK number found
	 */
	public int getMaxSDK() {
		return maxSDK;
	}
	
	/**
	 * @return an Integer Array containing all SDK versions available
	 */
	public Integer[] getSDKVersions() {
		Integer[] arr = availableSDKs.toArray(new Integer[availableSDKs.size()]);
		Arrays.sort(arr);
		return arr;
	}
	
	/**
	 * @param version
	 * @return the path in the file system containing the Android SDK with the given version
	 */
	public File getPath(int version) {
		return new File(platformPath.getAbsolutePath() + "/android-" + version);
	}
	
	/**
	 * analyzes the directoy and sets all variables accordingly
	 */
	private void analyze() {
		for(String directory : platformPath.getAbsoluteFile().list()) {
			if(directory.startsWith("android-")) {
				String version = directory.split("-")[1];
				try {
					Integer versionNumber = Integer.parseInt(version);
					availableSDKs.add(versionNumber);
					if(versionNumber > maxSDK) {
						maxSDK = versionNumber;
					}
				} catch(NumberFormatException e) {
					// preview SDKs sometimes have non-numerical names. simply ignore them
				}
			}
		}
	}
}
