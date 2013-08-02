/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/**
 * eXist LexBIG-WS Module
 * Copyright (c) 2005-2007 CancerGrid Consortium <http://www.cancergrid.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,  
 * and to permit persons to whom the Software is furnished to do so, subject to the 
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package org.cancergrid.xquery.modules.lexbigws;

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;


/**
 * eXist LexBIG-WS Module
 * 
 * An extension module for the eXist Native XML Database that add the ability to use query 
 * cancergrid LexBIG-WS.
 * 
 * @author Andrew Tsui <andrew.tsui@comlab.ox.ac.uk>
 * @serial 2007-11-08
 * @version 1.0
 *
 * @see org.exist.xquery.AbstractInternalModule#AbstractInternalModule(org.exist.xquery.FunctionDef[])
 */
public class LexBIGWSModule extends AbstractInternalModule
{
	public final static String NAMESPACE_URI = "http://cancergrid.org/xquery/lexbig-ws";
	
	public final static String PREFIX = "lexbig-ws";
	
	private final static FunctionDef[] functions = {new FunctionDef(ResolveSetFunction.signature, ResolveSetFunction.class)};
	
	public LexBIGWSModule() {
		super(functions);
	}

	public String getNamespaceURI() {
		return NAMESPACE_URI;
	}

	public String getDefaultPrefix() {
		return PREFIX;
	}

	public String getDescription() {
		return "An LexBIG-WS module that adds the ability to query cancergrid LexBIG-WS";
	}
}
