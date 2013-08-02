/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/**
 * eXist Web Service Client Module
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

package org.cancergrid.xquery.modules.webservice;
import org.exist.memtree.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
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
import org.exist.xquery.value.ValueSequence;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * eXist Web Service Client Module Extension RPCFunction 
 * 
 * Execute a synchronous RPC call against a web service (SOAP)
 * 
 * @author Andrew Tsui <andrew.tsui@comlab.ox.ac.uk>
 * @serial 2007-03-21
 * @version 1.0
 *
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext, org.exist.xquery.FunctionSignature)
 */
public class RPCFunction  extends BasicFunction
{
	public final static FunctionSignature signature = new FunctionSignature(
			new QName("rpc", WebServiceModule.NAMESPACE_URI, WebServiceModule.PREFIX),
			"A function to invoke a RPC-based web service. " +
			"Takes the web service URL for the $a, " +
			"the request XML for $b (follow the <methodName><param1>value</param1>...</methodName> pattern), " +
                  "and an optional SOAPAction value for $c.",
			new SequenceType[]{ 
				new SequenceType(Type.ANY_URI,	Cardinality.EXACTLY_ONE),
				new SequenceType(Type.NODE,	Cardinality.EXACTLY_ONE), 
				new SequenceType(Type.STRING,	Cardinality.ZERO_OR_ONE) }, 
			new SequenceType(Type.NODE,	Cardinality.ZERO_OR_ONE));
	
	public RPCFunction(XQueryContext context) {
		super(context, signature);
	}
	
	public Sequence eval(Sequence[] args, Sequence contextSequence)	throws XPathException {
		
		if (args[0].isEmpty())
			throw new XPathException("Undefined web service end point.");
		
		if (args[1].isEmpty())
			throw new XPathException("Undefined web service payload.");

		context.pushDocumentContext();
		try {
			String epr = args[0].itemAt(0).getStringValue();
			if(!epr.matches("^[a-z]+://.*"))
				throw new XPathException("Malformed URL Exception: " + epr);
			
			// Setup configuration for ServiceClient 
			// Dev note: maybe this can be store in XQueryContext for reuse?
			// Dev note2: ref sql module?
			Options options = new Options();
			options.setTo(new EndpointReference(epr));
            options.setTimeOutInMilliSeconds(60*1000);
			
			if (!args[2].isEmpty())
				options.setAction(args[2].getStringValue());
			
			ServiceClient sender = new ServiceClient();
			sender.setOptions(options);
			
			OMElement method = createMethodOM((NodeValue)args[1].itemAt(0));
			
			OMElement response = null;
			try 
 			{
				response = sender.sendReceive(method);
			} catch (NullPointerException e)
			{
				return Sequence.EMPTY_SEQUENCE;
			}
			if (response == null)
				return Sequence.EMPTY_SEQUENCE;
			
			//return getReturnList(response.getChildren());
			return (NodeValue)getResponseDoc(response).getDocumentElement();
		} catch (Exception e) {
			throw new XPathException(e.getMessage());
		} finally {
			context.popDocumentContext();
		}
	}
	
	/**
	 * Create the request OMElement from the given node. 
	 * @param nv node of the request XML
	 * @return OMElement of the request XMl
	 * @throws XPathException
	 */
	private OMElement createMethodOM(NodeValue nv) throws XPathException 
	{
		try {
			ExtendedDOMSerializer serializer = new ExtendedDOMSerializer(context.getBroker());
			StringWriter buffer = new StringWriter();
			serializer.setWriter(buffer);

			Properties properties = new Properties();
			properties.setProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "true");
			serializer.setOutputProperties(properties);
			serializer.serialize(nv.getNode());

			XMLStreamReader p = XMLInputFactory.newInstance().createXMLStreamReader(new java.io.StringReader(buffer.toString()));
			StAXOMBuilder builder = new StAXOMBuilder(p);
			return builder.getDocumentElement();
		} catch (TransformerException te)
		{
			
			throw new XPathException(te.getMessage());
		} catch (XMLStreamException xse)
		{
			throw new XPathException(xse.getMessage());
		}
	}
	
	/**
	 * The SOAP response may contain more than one return result. Iterate through the returned items list
	 * the create a NodeSet.
	 * 
	 * @param iterator Iterator created from the SOAP response OMElement
	 * @return A NodeSet containing one or more return items
	 * @throws XPathException
	 */
	private ValueSequence getReturnList(java.util.Iterator iterator) throws XPathException 
	{
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			MemTreeBuilder mtbuilder = context.getDocumentBuilder();
			DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(mtbuilder);
			reader.setContentHandler(receiver);

			ValueSequence result = new ValueSequence();
			Document doc;
			InputSource src; 
			while (iterator.hasNext())
			{
				OMElement item = (OMElement)iterator.next();
				src = new InputSource(new ByteArrayInputStream(item.toString().getBytes("UTF-8")));
				reader.parse(src);
				doc = receiver.getDocument();
				result.add((NodeValue)doc.getDocumentElement());
			}
			return result;
		} catch (ParserConfigurationException pce) {
			throw new XPathException(pce.getMessage());
		} catch (SAXException se) {
			throw new XPathException(se.getMessage());
		} catch (IOException ioe) {
			throw new XPathException(ioe.getMessage());
		}
	}
	
	private Document getResponseDoc(OMElement response) throws Exception
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
