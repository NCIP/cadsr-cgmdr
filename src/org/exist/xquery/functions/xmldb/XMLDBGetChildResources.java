/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 Wolfgang M. Meier
 *  wolfgang@exist-db.org
 *  http://exist.sourceforge.net
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * $Id: XMLDBGetChildResources.java 3457 2006-05-07 14:46:21Z wolfgang_m $
 *  
 */
package org.exist.xquery.functions.xmldb;

import org.exist.dom.QName;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;



public class XMLDBGetChildResources extends XMLDBAbstractCollectionManipulator {

	public final static FunctionSignature signature =
		new FunctionSignature(
			new QName("get-child-resources", XMLDBModule.NAMESPACE_URI, XMLDBModule.PREFIX),
			"Returns all child resources in the specified collection.",
			new SequenceType[] {
					new SequenceType(Type.ITEM, Cardinality.EXACTLY_ONE)
			},
			new SequenceType(Type.STRING, Cardinality.ZERO_OR_MORE));
	
	public XMLDBGetChildResources(XQueryContext context) {
		super(context, signature);
	}
	
	//TODO decode names?
	public Sequence evalWithCollection(Collection collection, Sequence[] args, Sequence contextSequence)
		throws XPathException {
		ValueSequence result = new ValueSequence();
		try {
			String[] collections = collection.listResources();
			for(int i = 0; i < collections.length; i++) {
				result.add(new StringValue(collections[i]));
			}
			return result;
		} catch (XMLDBException e) {
			throw new XPathException(getASTNode(), "Failed to retrieve child resources", e);
		}
	}
}
