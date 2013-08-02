/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.client.xacml;

public abstract class AbstractNodeContainer extends AbstractTreeNode implements NodeContainer
{
	public AbstractNodeContainer(NodeContainer parent)
	{
		super(parent);
	}
	public void nodeChanged(XACMLTreeNode node)
	{
		NodeContainer parent = getParent();
		if(parent != null)
			parent.nodeChanged(node);
	}
	public void nodeAdded(XACMLTreeNode node, int newIndex)
	{
		NodeContainer parent = getParent();
		if(parent != null)
			parent.nodeAdded(node, newIndex);
	}
	public void nodeRemoved(XACMLTreeNode removedNode, int oldChildIndex)
	{
		NodeContainer parent = getParent();
		if(parent != null)
			parent.nodeRemoved(removedNode, oldChildIndex);
	}
}
