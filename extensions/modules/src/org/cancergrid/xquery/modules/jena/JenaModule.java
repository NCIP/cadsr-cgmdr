/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/**
 * eXist Jena Module
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

package org.cancergrid.xquery.modules.Jena;

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;


/**
 * eXist Jena Module
 * 
 * An extension module for the eXist Native XML Database that add the ability to use SPARQL
 * to query RDF/OWL XML.
 * 
 * @author Andrew Tsui <andrew.tsui@comlab.ox.ac.uk>
 * @serial 2007-07-31
 * @version 1.0
 *
 * @see org.exist.xquery.AbstractInternalModule#AbstractInternalModule(org.exist.xquery.FunctionDef[])
 */
public class JenaModule  extends AbstractInternalModule
{
	public final static String NAMESPACE_URI = "http://cancergrid.org/xquery/jena";
	
	public final static String PREFIX = "jena";
	
	private final static FunctionDef[] functions = {
		new FunctionDef(SparqlFunction.signature[0], SparqlFunction.class),
		new FunctionDef(SparqlFunction.signature[1], SparqlFunction.class),
		new FunctionDef(SparqlFunction.signature[2], SparqlFunction.class),
		new FunctionDef(GenerateInfModelFunction.signature[0], GenerateInfModelFunction.class),
		new FunctionDef(GenerateInfModelFunction.signature[1], GenerateInfModelFunction.class),
		new FunctionDef(GenerateInfModelFunction.signature[2], GenerateInfModelFunction.class)
	};
	
	public JenaModule() {
		super(functions);
	}

	public String getNamespaceURI() {
		return NAMESPACE_URI;
	}

	public String getDefaultPrefix() {
		return PREFIX;
	}

	public String getDescription() {
		return "An Jena module that adds the ability to use SPARQL to query RDF/OWL XML. Powered by Jena Semantic Web Framework.";
	}
}
