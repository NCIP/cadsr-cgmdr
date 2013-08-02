/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.dom;

/**
 * Holds a mutable reference to a NodeImpl, used to pass a node by reference.
 *
 * @author <a href="mailto:piotr@ideanest.com">Piotr Kaminski</a>
 */
public class NodeImplRef {	
	
    public NodeImplRef() {this.node = null;}
    public NodeImplRef(StoredNode node) {this.node = node;}
    public void setNode(StoredNode node) {this.node = node;}    
    public StoredNode getNode() {return this.node;}
	
	private StoredNode node;

}
