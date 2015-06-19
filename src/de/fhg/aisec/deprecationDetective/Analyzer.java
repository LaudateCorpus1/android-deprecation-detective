package de.fhg.aisec.deprecationDetective;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * 
 * @author Michael Eder (michael.eder@aisec.fraunhofer.de)
 *
 */
public class Analyzer {
	private static Logger log;
	public static List<Class<?>> getDeprecatedClasses(File path) {
		log = Logger.getLogger("DeprecationDetective");
		List<Class<?>> classes = new LinkedList<Class<?>>();
		Path tempDir = unzip(path + "/android.jar");
		for (File classFile : findClasses(tempDir.toFile())) {
			try {
				Class<?> c = getClassFromFile(tempDir.toString() + "/", classFile.toString().replace(tempDir.toString() + "/", "").replace("/", "."));
				if(c.isAnnotationPresent(java.lang.Deprecated.class)) {
					classes.add(c);					
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Something with the classloader and the class paths went wrong. Aborting!");
				System.exit(1);
			} 
		}
		deleteDirectory(tempDir.toFile());
		return classes;
	}
	
	public static List<Class<?>> getNonDeprecatedClasses(File path) {
		log = Logger.getLogger("DeprecationDetective");
		List<Class<?>> classes = new LinkedList<Class<?>>();
		Path tempDir = unzip(path + "/android.jar");
		for (File classFile : findClasses(tempDir.toFile())) {
			try {
				Class<?> c = getClassFromFile(tempDir.toString() + "/", classFile.toString().replace(tempDir.toString() + "/", "").replace("/", "."));
				if(!c.isAnnotationPresent(java.lang.Deprecated.class)) {
					classes.add(c);					
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Something with the classloader and the class paths went wrong. Aborting!");
				System.exit(1);
			} 
		}
		deleteDirectory(tempDir.toFile());
		return classes;
	}

	/**
	 * Unzips the file located at path to a directory in the systems temp folder.
	 * The path of the directory is returned.
	 * @param path
	 * @return
	 */
	private static Path unzip(String path) {
		String source = path.toString();
		Path destination = null;
		try {
			destination = Files.createTempDirectory("androidPlatform_unpacked");
		} catch (IOException e1) {
			log.log(Level.SEVERE, "Could not create temp directory for extracting the Android platform. Aborting.");
			System.exit(1);
		}
		try {
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(destination.toString());
			return destination;
		} catch (ZipException e) {
			log.log(Level.SEVERE, "jar file not found. Aborting.");
			System.exit(1);
		}
		return null;
	}

	/**
	 * Deletes a directory, even if it is not empty
	 * @param path
	 */
	private static void deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		path.delete();
	}
	
	/**
	 * Returns a list containing all Class files (*.class) in the
	 * given directory
	 */
	private static List<File> findClasses(File directory) {
		ArrayList<File> files = new ArrayList<File>();
		for (File f : directory.listFiles()) {
			if (f.isDirectory()){
				files.addAll(findClasses(f));
			}
			else {
				if(f.getName().endsWith(".class")) {
					files.add(f);
				}
			}
		}
		return files;
	}
	
	/**
	 * Load class from a given .class file
	 * @param directory path to the directory containing the classfile
	 * @param className classname ending with ".class" 
	 * @return the class contained in the classfile
	 * @throws Exception
	 */
	private static Class<?> getClassFromFile(String directory, String className) throws Exception {
	    URLClassLoader loader = new URLClassLoader(new URL[] {
	            new URL("file://" + directory)
	    });
	    Class<?> cla = loader.loadClass(className.replace(".class", ""));
	    loader.close();
	    return cla;
	}
	
	@SuppressWarnings("unused")
	private static ClassLoader getClassLoaderFromJar(String directory) throws Exception {
		return new URLClassLoader(new URL[] {
	            new URL("file://" + directory)
	    });
	}
}
