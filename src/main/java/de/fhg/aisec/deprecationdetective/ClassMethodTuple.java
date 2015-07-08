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
import java.lang.reflect.Executable;

/**
 * This class is used for storing the mapping between a class and a certain method
 * that is available in this class. This needs to be done because only having the
 * method object extracted from the class and passing it around leads to information loss
 * because m.getClass() returns a Method object and m.getDeclaringClass() returns the superclass
 * or interface the method is declared in. 
 * 
 * @author Michael Eder (michael.eder@aisec.fraunhofer.de)
 *
 */
public class ClassMethodTuple {
	private Class<?> classMethodIsAvailableIn;
	private Executable accordingMethod;
	
	public ClassMethodTuple(Class<?> classMethodIsAvailableIn, Executable accordMethod) {
		this.classMethodIsAvailableIn = classMethodIsAvailableIn;
		this.accordingMethod = accordMethod;
	}

	public Class<?> getClassMethodIsAvailableIn() {
		return classMethodIsAvailableIn;
	}

	public Executable getAccordingMethod() {
		return accordingMethod;
	}

	@Override
	public String toString() {
		return classMethodIsAvailableIn.getName() + "." + accordingMethod.getName();
	}
	
	

}
