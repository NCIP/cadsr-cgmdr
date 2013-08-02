/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.security;

import java.security.Principal;

/**
 * @author mdiggory
 */
public interface XmldbPrincipal extends Principal {

	public String getName();
	
	public String getPassword();
	
	public boolean hasRole(String role);

}