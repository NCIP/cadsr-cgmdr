/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.client.xacml;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

public interface NodeEditor
{
	JComponent getComponent();
	void setNode(XACMLTreeNode node);
	void pushChanges();

	void removeChangeListener(ChangeListener listener);
	void addChangeListener(ChangeListener listener);
}
