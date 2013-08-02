/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.client.xacml;

import com.sun.xacml.PolicyTreeElement;

public interface PolicyElementContainer extends NodeContainer
{
	void add(PolicyElementNode node);
	void add(int index, PolicyElementNode node);
	void add(PolicyTreeElement element);
	void add(int index, PolicyTreeElement element);
	
	void remove(PolicyElementNode node);
	
	boolean containsId(String id);
}
