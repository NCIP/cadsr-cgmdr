/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.dom;

public interface ByDocumentIterator {

	public void nextDocument(DocumentImpl document);
	
	public boolean hasNextNode();
	
	public NodeProxy nextNode();
    
    public NodeProxy peekNode();

    public void setPosition(NodeProxy node);
}
