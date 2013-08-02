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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;

import org.cancergrid.lexbig.CombinatorialQueryStub;
import org.cancergrid.schema.resultSet.AllNames;
import org.cancergrid.schema.resultSet.Concept;
import org.cancergrid.schema.resultSet.Names;
import org.cancergrid.schema.resultSet.Properties;
import org.cancergrid.schema.resultSet.Property;
import org.cancergrid.schema.resultSet.ResultSet;
import org.cancergrid.schema.resultSet.ResultSetDocument;
import org.exist.dom.QName;
import org.exist.memtree.DocumentBuilderReceiver;
import org.exist.memtree.MemTreeBuilder;
import org.exist.util.serializer.ExtendedDOMSerializer;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * eXist LexBIG-WS Module Extension ResolveSetFunction
 * 
 * Execute a SPARQL query against OWL content
 * 
 * @author Andrew Tsui <andrew.tsui@comlab.ox.ac.uk>
 * @serial 2007-11-08
 * @version 1.0
 * 
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext,
 *      org.exist.xquery.FunctionSignature)
 */
public class ResolveSetFunction extends BasicFunction {
	public final static FunctionSignature signature =	
		new FunctionSignature(
			new QName("resolve-set", LexBIGWSModule.NAMESPACE_URI, LexBIGWSModule.PREFIX),
				"A function to query cancergrid LexBIG-WS. " +
				"Takes the LexBIG-WS service endpoint URL for $a, " +
				"and query formated as XML node for $b",
			new SequenceType[] {
				new SequenceType(Type.ANY_URI, Cardinality.EXACTLY_ONE),
				new SequenceType(Type.NODE, Cardinality.EXACTLY_ONE)
				},
			new SequenceType(Type.NODE, Cardinality.ZERO_OR_ONE)
		);
	
	public ResolveSetFunction(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence)
			throws XPathException {

		if (args[0].isEmpty())
			throw new XPathException("Undefined web service end point.");
		
		if (args[1].isEmpty())
			throw new XPathException("Undefined query.");


		context.pushDocumentContext();
		try {
			String epr = args[0].itemAt(0).getStringValue();
			if(!epr.matches("^[a-z]+://.*"))
				throw new XPathException("Malformed URL Exception: " + epr);
			
			CombinatorialQueryStub stub = new CombinatorialQueryStub(epr);
			CombinatorialQueryStub.ResolveSet resolveSet = createResolveSet(((NodeValue)args[1].itemAt(0)));
			CombinatorialQueryStub.ResolveSetResponse response = stub.resolveSet(resolveSet);
			CombinatorialQueryStub.ResolvedConceptReference[] conceptRefList = response.get_return().getResolvedConceptReference();
			if (conceptRefList.length == 0)
			{
				return Sequence.EMPTY_SEQUENCE;
			}
			
			ResultSetDocument resultSetDoc = ResultSetDocument.Factory.newInstance();
			ResultSet resultSet = resultSetDoc.addNewResultSet();
			for (CombinatorialQueryStub.ResolvedConceptReference conceptRef : conceptRefList)
			{
				Concept concept = resultSet.addNewConcept();
				Names names = concept.addNewNames();
				Properties props = concept.addNewProperties();
				
				names.setId("GB-CANCERGRID-LEXBIG-" + conceptRef.getConceptCode());
				
				CombinatorialQueryStub.CodedEntry entry = conceptRef.getReferencedEntry();
				CombinatorialQueryStub.Definition[] definitions = entry.getDefinition();
				
				for (CombinatorialQueryStub.Definition d : definitions)
				{
					try 
					{
						if (d.getIsPreferred() == true)
						{
							if (d.getText().getContent() != null)
								concept.setDefinition(d.getText().getContent());
							else 
								concept.setDefinition("(No definition supplied)");
							break;
						}
					} catch (Exception ex)
					{
						//For some reason, d can be null.
					}
				}
				
				CombinatorialQueryStub.Presentation[] presentations =  entry.getPresentation();
				AllNames allNames = names.addNewAllNames();
				for (CombinatorialQueryStub.Presentation p : presentations)
				{
					if (p.getRepresentationalForm().equals("Preferred_Name"))
					{
						names.setPreferred(p.getText().getContent());
						continue;
					}
					
					if (!Arrays.asList(allNames.getNameArray()).contains(p.getText().getContent()))
					{
						allNames.addName(p.getText().getContent());
					}
				}

				CombinatorialQueryStub.Property[] properties = entry.getProperty();
				for (CombinatorialQueryStub.Property p : properties)
				{
					Property prop = props.addNewProperty();
					prop.setName(p.getProperty());
					prop.setValue(p.getText().getContent());
				}
			}
			
			return (NodeValue) getResponseDoc(resultSetDoc).getDocumentElement();
		} catch (Exception e) {
			throw new XPathException(e.getMessage());
		} finally {
			context.popDocumentContext();
		}
	}
	
	/**
	 * Create the request OMElement from the given node. 
	 * @param nv node of the request XML
	 * @return OMElement of the request XML
	 * @throws XPathException
	 */
	private CombinatorialQueryStub.ResolveSet createResolveSet(NodeValue nv) throws XPathException 
	{
		try {
			ExtendedDOMSerializer serializer = new ExtendedDOMSerializer(context.getBroker());
			StringWriter buffer = new StringWriter();
			serializer.setWriter(buffer);

			java.util.Properties properties = new java.util.Properties();
			properties.setProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "true");
			serializer.setOutputProperties(properties);
			serializer.serialize(nv.getNode());

			XMLStreamReader p = XMLInputFactory.newInstance().createXMLStreamReader(new java.io.StringReader(buffer.toString()));
			return CombinatorialQueryStub.ResolveSet.Factory.parse(p);
		} 
		catch (TransformerException te)
		{
			throw new XPathException(te.getMessage());
		} 
		catch (XMLStreamException xse)
		{
			throw new XPathException(xse.getMessage());
		}
		catch (Exception e)
		{
			throw new XPathException(e.getMessage());
		}
	}
	
	private Document getResponseDoc(ResultSetDocument response) throws Exception
	{
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		MemTreeBuilder mtbuilder = context.getDocumentBuilder();
		DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(mtbuilder);
		reader.setContentHandler(receiver);
		
		InputSource src = new InputSource(new ByteArrayInputStream(response.toString().getBytes("UTF-8")));
		reader.parse(src);
		Document doc = receiver.getDocument();
		return doc;
	}

}
