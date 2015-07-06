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
import java.io.IOException;
import java.lang.reflect.Method;
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
 * @author 	Michael Eder	(michael.eder@aisec.fraunhofer.de), 
 * 			Julian Schütte	(julian.schuette@aisec.fraunhofer.de)
 *
 */
public class Analyzer {
	private static Logger log;
	private Path tempDir;
	private File sdkPath;

	public Analyzer(File path) {
		log = Logger.getLogger("DeprecationDetective");
		sdkPath = path;
		tempDir = unzip(sdkPath + "/android.jar");
	}

	/**
	 * Deletes the temporary directory that stores the unzipped jar contents. If
	 * you like your RAM, consider calling this method when you're done ;)
	 */
	public void cleanUp() {
		deleteDirectory(tempDir.toFile());
	}

	/**
	 * Returns a list containing all classes with the @Deprecated annotation set
	 * or not
	 * 
	 * @param deprecated
	 *            control if classes that are deprecated or not are returned.
	 * @return
	 */
	private List<Class<?>> getClasses(boolean deprecated) {
		List<Class<?>> listOfClasses = new LinkedList<Class<?>>();
		for (File classFile : findClasses(tempDir.toFile())) {
			try {
				Class<?> c = getClassFromFile(tempDir.toString() + "/", classFile.toString().replace(tempDir.toString() + "/", "").replace("/", "."));
				if (c.isAnnotationPresent(java.lang.Deprecated.class) == deprecated) {
					listOfClasses.add(c);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Something with the classloader and the class paths went wrong. Aborting!");
				System.exit(1);
			}
		}
		return listOfClasses;
	}

	/**
	 * @return A list of all classes that are labeled with the @Deprecated
	 *         Annotation
	 */
	public List<Class<?>> getDeprecatedClasses() {
		return getClasses(true);
	}

	/**
	 * @return A list of all classes that are not labeled with the @Deprecated
	 *         Annotation
	 */
	public List<Class<?>> getNonDeprecatedClasses() {
		return getClasses(false);
	}

	/**
	 * Returns a list containing all methods with the @Deprecated annotation set
	 * or not
	 * 
	 * @param deprecated
	 *            control if methods that are deprecated or not are returned.
	 * @return
	 */
	private List<Method> getMethods(boolean deprecated) {
		List<Method> listOfMethods = new LinkedList<Method>();
		ClassLoader androidjar = null;
		try {
			androidjar = getClassLoaderFromJar(sdkPath + "/android.jar");
		} catch (Exception e) {
			log.log(Level.SEVERE, "Something went wrong loading the android jar into the classloader. Aborting", e);
			System.exit(1);
		}
		for (File classFile : findClasses(tempDir.toFile())) {
			try {
				Class<?> c = getClassFromFile(tempDir.toString() + "/", classFile.toString().replace(tempDir.toString() + "/", "").replace("/", "."));
				if (androidjar != null) {
					Class<?> classWithContext = androidjar.loadClass(c.getName());
					for (Method method : classWithContext.getDeclaredMethods()) {
						if (method.isAnnotationPresent(java.lang.Deprecated.class) == deprecated) {
							listOfMethods.add(method);
						}
					}
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Something with the classloader and the class paths went wrong. Aborting!", e);
				System.exit(1);
			}
		}
		return listOfMethods;
	}

	/**
	 * @return A list of all methods that are labeled with the @Deprecated
	 *         Annotation
	 */
	public List<Method> getDeprecatedMethods() {
		return getMethods(true);
	}

	/**
	 * @return A list of all methods that are not labeled with the @Deprecated
	 *         Annotation
	 */
	public List<Method> getNonDeprecatedMethods() {
		return getMethods(false);
	}

	/**
	 * Unzips the file located at path to a directory in the systems temp
	 * folder. The path of the directory is returned.
	 * 
	 * @param path
	 * @return
	 */
	private static Path unzip(String path) {
		String source = path.toString();
		Path destination = null;
		try {
			destination = Files.createTempDirectory("androidPlatform_unpacked");
		} catch (IOException e1) {
			log.log(Level.SEVERE, "Could not create temp directory for extracting the Android platform. Aborting.", e1);
			System.exit(1);
		}
		try {
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(destination.toString());
			return destination;
		} catch (ZipException e) {
			log.log(Level.SEVERE, "jar file not found. Aborting.", e);
			System.exit(1);
		}
		return null;
	}

	/**
	 * Deletes a directory, even if it is not empty
	 * 
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
	 * Returns a list containing all Class files (*.class) in the given
	 * directory
	 */
	private static List<File> findClasses(File directory) {
		ArrayList<File> files = new ArrayList<File>();
		for (File f : directory.listFiles()) {
			if (f.isDirectory()) {
				files.addAll(findClasses(f));
			} else {
				if (f.getName().endsWith(".class")) {
					files.add(f);
				}
			}
		}
		return files;
	}

	/**
	 * Load class from a given .class file
	 * 
	 * @param directory
	 *            path to the directory containing the classfile
	 * @param className
	 *            classname ending with ".class"
	 * @return the class contained in the classfile
	 * @throws Exception
	 */
	private static Class<?> getClassFromFile(String directory, String className) throws Exception {
		URLClassLoader loader = new URLClassLoader(new URL[] { new URL("file://" + directory) });
		Class<?> cla = loader.loadClass(className.replace(".class", ""));
		loader.close();
		return cla;
	}

	/**
	 * Loads a complete jar and returns the ClassLoader.
	 * 
	 * @param directory
	 * @return
	 * @throws Exception
	 */
	private static ClassLoader getClassLoaderFromJar(String directory) throws Exception {
		return new URLClassLoader(new URL[] { new URL("file://" + directory) });
	}

}
