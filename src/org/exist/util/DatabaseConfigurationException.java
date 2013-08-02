/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.util;

public class DatabaseConfigurationException extends Exception {

    public DatabaseConfigurationException() {
		super();
	}

	public DatabaseConfigurationException(String message) {
		super(message);
	}

    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
