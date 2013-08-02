/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.client.xacml;

public interface NodeChangeListener
{
	void nodeChanged(XACMLTreeNode node);
	void nodeAdded(XACMLTreeNode node, int newIndex);
	void nodeRemoved(XACMLTreeNode removedNode, int oldChildIndex);
}
