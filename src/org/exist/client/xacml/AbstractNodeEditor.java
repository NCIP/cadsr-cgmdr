/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.client.xacml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class AbstractNodeEditor implements NodeEditor
{
	private List listeners = new ArrayList(2);

	protected void fireChanged()
	{
		ChangeEvent event = new ChangeEvent(this);
		for(Iterator it = listeners.iterator(); it.hasNext();)
			((ChangeListener)it.next()).stateChanged(event);
	}
	public void addChangeListener(ChangeListener listener)
	{
		if(listener != null)
			listeners.add(listener);
	}
	public void removeChangeListener(ChangeListener listener)
	{
		if(listeners != null)
			listeners.remove(listeners);
	}
}
