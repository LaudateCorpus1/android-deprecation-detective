package de.fhg.aisec.deprecationDetective;

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
