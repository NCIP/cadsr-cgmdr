/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-06 The eXist Project
 *  http://exist-db.org
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
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *  $Id: FileRead.java 3334 2006-04-27 15:16:13Z wolfgang_m $
 */
package org.exist.xquery.functions.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.exist.dom.QName;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/**
 * @author Pierrick Brihaye
 * @author Dizzzz
 *
 */
public class FileRead extends BasicFunction {

	public final static FunctionSignature signatures[] = {
		new FunctionSignature(
			new QName("file-read", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
			"Read content of file $a",
			new SequenceType[] {				
				new SequenceType(Type.ITEM, Cardinality.EXACTLY_ONE)
			},				
			new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE)),
		new FunctionSignature(
			new QName("file-read", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
			"Read content of file $a with the encoding specified in $b.",
			new SequenceType[] {
				new SequenceType(Type.ITEM, Cardinality.EXACTLY_ONE),
				new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
			},
			new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE))
	};
	
	/**
	 * @param context
	 * @param signature
	 */
	public FileRead(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.BasicFunction#eval(org.exist.xquery.value.Sequence[], org.exist.xquery.value.Sequence)
	 */
	public Sequence eval(Sequence[] args, Sequence contextSequence)
			throws XPathException {
		String arg = args[0].itemAt(0).getStringValue();
		StringWriter sw;
		try {
			URL url = new URL(arg);
			InputStreamReader isr;
			if (args.length > 1)			
				isr = new InputStreamReader(url.openStream(), arg = args[1].itemAt(0).getStringValue());
			else
				isr = new InputStreamReader(url.openStream());
			sw = new StringWriter();
			char[] buf = new char[1024];
	        int len;
	        while ((len = isr.read(buf)) > 0) {
	            sw.write(buf, 0, len);
	        }
			isr.close();
			sw.close();
		} catch (MalformedURLException e) {
			throw new XPathException(getASTNode(), e.getMessage());	
		} catch (IOException e) {
			throw new XPathException(getASTNode(), e.getMessage());	
		}
		//TODO : return an *Item* built with sw.toString()
		return new StringValue(sw.toString());
	}
}
