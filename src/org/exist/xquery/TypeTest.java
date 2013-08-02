/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2001-2007 The eXist Project
 * http://exist-db.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *  
 *  $Id: TypeTest.java 7743 2008-05-09 21:46:12Z wolfgang_m $
 */
package org.exist.xquery;

import org.exist.dom.NodeProxy;
import org.exist.dom.QName;
import org.exist.xquery.value.Type;
import org.w3c.dom.Node;

import javax.xml.stream.XMLStreamReader;

/**
 * Tests if a node is of a given node type.
 * 
 * @author Wolfgang Meier (wolfgang@exist-db.org)
 */
public class TypeTest implements NodeTest {

    protected int nodeType = 0;
	
    public TypeTest(int nodeType) {
        setType(nodeType);
    }

    public void setType(int nodeType) {
        this.nodeType = nodeType;
    }
	
    public int getType() {
        return nodeType;
    }
	
    public QName getName() {
        return null;
    }
	
    protected boolean isOfType(short type) {
        int domType;
        switch (nodeType) {
            case Type.ELEMENT :
                domType = Node.ELEMENT_NODE;
                break;
            case Type.TEXT :
                domType = Node.TEXT_NODE;
                break;
            case Type.ATTRIBUTE :
                domType = Node.ATTRIBUTE_NODE;
                break;
            case Type.COMMENT :
                domType = Node.COMMENT_NODE;
                break;
            case Type.PROCESSING_INSTRUCTION :
                domType = Node.PROCESSING_INSTRUCTION_NODE;
                break;
            case Type.NODE :
            default :
                return true;
        }
        if (type == Node.CDATA_SECTION_NODE)
            type = Node.TEXT_NODE;
        return (type == domType);
    }

    protected boolean isOfEventType(int type) {
        if (nodeType == Type.NODE)
            return true;
        int xpathType;
        switch (type) {
            case XMLStreamReader.START_ELEMENT :
                xpathType = Type.ELEMENT;
                break;
            case XMLStreamReader.ATTRIBUTE :
                xpathType = Type.ATTRIBUTE;
                break;
            case XMLStreamReader.CHARACTERS :
            case XMLStreamReader.CDATA :
                xpathType = Type.TEXT;
                break;
            case XMLStreamReader.COMMENT :
                xpathType = Type.COMMENT;
                break;
            case XMLStreamReader.PROCESSING_INSTRUCTION :
                xpathType = Type.PROCESSING_INSTRUCTION;
                break;
            default:
                return false;
        }
        return xpathType == nodeType;
    }

    public String toString() {
        return nodeType == Type.NODE ? "node()" : Type.NODETYPES[nodeType] + "()";
    }
	
    /* (non-Javadoc)
     * @see org.exist.xquery.NodeTest#matches(org.exist.dom.NodeProxy)
     */
    public boolean matches(NodeProxy proxy) {
        short otherNodeType = proxy.getNodeType();
        if(otherNodeType == Type.ITEM || otherNodeType == Type.NODE) {
            //TODO : what are the semantics of Type.NODE ?
            if (this.nodeType == Type.NODE)
                return true;	
            return isOfType(proxy.getNode().getNodeType());
        } else
            return isOfType(otherNodeType);
    }
    /* (non-Javadoc)
     * @see org.exist.xquery.NodeTest#matches(org.exist.dom.NodeProxy)
     */
    public boolean matches(Node other) {
        if(other == null)
            return false;
        return isOfType(other.getNodeType());
    }

    public boolean matches(XMLStreamReader reader) {
        return isOfEventType(reader.getEventType());
    }

    /* (non-Javadoc)
     * @see org.exist.xquery.NodeTest#isWildcardTest()
     */
    public boolean isWildcardTest() {
        return true;
    }

}
