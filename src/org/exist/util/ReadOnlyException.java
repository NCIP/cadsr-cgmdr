/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.util;


public class ReadOnlyException extends Exception {

	/**
	 * Constructor for ReadOnlyException.
	 */
	public ReadOnlyException() {
		super();
	}

	/**
	 * Constructor for ReadOnlyException.
	 * @param message
	 */
	public ReadOnlyException(String message) {
		super(message);
	}

}
