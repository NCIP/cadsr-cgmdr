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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

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

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * eXist Jena Module Extension SparqlFunction
 * 
 * Execute a SPARQL query against OWL content
 * 
 * @author Andrew Tsui <andrew.tsui@comlab.ox.ac.uk>
 * @serial 2007-07-31
 * @version 1.0
 * 
 * @see org.exist.xquery.BasicFunction#BasicFunction(org.exist.xquery.XQueryContext,
 *      org.exist.xquery.FunctionSignature)
 */
public class GenerateInfModelFunction extends BasicFunction {
	public final static FunctionSignature[] signature = 
	{
		new FunctionSignature(
			new QName("generate", JenaModule.NAMESPACE_URI, JenaModule.PREFIX),
				"A function to generate RDFS InfModel from RDF/OWL XML. " +
				"Takes the OWL content as XML node for $a.",
			new SequenceType[] {
					new SequenceType(Type.NODE, Cardinality.EXACTLY_ONE),
					},
			new SequenceType(Type.NODE, Cardinality.ZERO_OR_ONE)
		),
		
		new FunctionSignature(
				new QName("generate", JenaModule.NAMESPACE_URI, JenaModule.PREFIX),
					"A function to generate Rule InfModel from RDF/OWL XML. " +
					"Takes the OWL content as XML node for $a, and " +
					"a set of rules in xs:string format for $b. ",
				new SequenceType[] {
						new SequenceType(Type.NODE, Cardinality.EXACTLY_ONE),
						new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
						},
				new SequenceType(Type.NODE, Cardinality.ZERO_OR_ONE)
		),
			
		new FunctionSignature(
				new QName("generate", JenaModule.NAMESPACE_URI, JenaModule.PREFIX),
				"A function to generate Rule InfModel from RDF/OWL XML. " +
				"Takes the OWL content as XML node for $a, " +
				"a set of rules in xs:string format for $b, " +
				"and what mode the rule reasoner should run in for $c. " +
				"The modes incluses: FORWARD, FORWARD_RETE, BACKWARD, and HYBRID (default)",
				new SequenceType[] {
						new SequenceType(Type.NODE, Cardinality.EXACTLY_ONE),
						new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE),
						new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
						},
				new SequenceType(Type.NODE, Cardinality.ZERO_OR_ONE)
			)
		
	};
	
	public GenerateInfModelFunction(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	public Sequence eval(Sequence[] args, Sequence contextSequence)
			throws XPathException {

		if (args[0].isEmpty())
			throw new XPathException("Undefined RDF/OWL XML.");

		if (args.length >= 2 && args[1].isEmpty())
			throw new XPathException("Undefined rules set.");
		
		if (args.length == 3 && args[2].isEmpty())
			throw new XPathException("Undefined rule mode.");

		context.pushDocumentContext();
		try {
			Reader content = getContentAsReader((NodeValue) args[0].itemAt(0));
			Model model = ModelFactory.createDefaultModel();
			model.read(content, null);
			
			InfModel infModel = null;
			
			if (args.length >= 2 && !args[1].isEmpty())
			{
				String rules = args[1].getStringValue();
				GenericRuleReasoner ruleReasoner = new GenericRuleReasoner(Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(new StringReader(rules)))));
				//ruleReasoner.setOWLTranslation(true);
				ruleReasoner.setTransitiveClosureCaching(true);
				
				if (args.length == 3 && !args[2].isEmpty())
				{
					String ruleMode = args[2].getStringValue();
					if (ruleMode.equalsIgnoreCase("BACKWARD"))
					{
						ruleReasoner.setMode(GenericRuleReasoner.BACKWARD);
					} 
					else if (ruleMode.equalsIgnoreCase("FORWARD"))
					{
						ruleReasoner.setMode(GenericRuleReasoner.FORWARD);
					}
					else if (ruleMode.equalsIgnoreCase("FORWARD_RETE"))
					{
						ruleReasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
					}
					else
					{
						ruleReasoner.setMode(GenericRuleReasoner.HYBRID);
					}
				}
				
				infModel = ModelFactory.createInfModel(ruleReasoner, model);
			} else
			{
				Reasoner rdfsReasoner = ReasonerRegistry.getRDFSReasoner();
				infModel = ModelFactory.createInfModel(rdfsReasoner, model);
			}
			StringWriter writer = new StringWriter();
			infModel.write(writer);
			writer.flush();
			String inf = writer.toString();
			writer.close();
			return (NodeValue) getResponseDoc(inf).getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
			throw new XPathException(e.getMessage());
		} finally {
			context.popDocumentContext();
		}
	}

	private Reader getContentAsReader(NodeValue nv) throws XPathException {
		try {
			ExtendedDOMSerializer serializer = new ExtendedDOMSerializer(
					context.getBroker());
			StringWriter buffer = new StringWriter();
			serializer.setWriter(buffer);

			Properties properties = new Properties();
			properties
					.setProperty(
							javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION,
							"true");
			serializer.setOutputProperties(properties);
			serializer.serialize(nv.getNode());

			StringReader r = new java.io.StringReader(buffer.toString());
			return r;
		} catch (TransformerException te) {
			throw new XPathException(te.getMessage());
		}
	}

	private Document getResponseDoc(String response) throws Exception {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();
		MemTreeBuilder mtbuilder = context.getDocumentBuilder();
		DocumentBuilderReceiver receiver = new DocumentBuilderReceiver(
				mtbuilder);
		reader.setContentHandler(receiver);

		InputSource src = new InputSource(new ByteArrayInputStream(response
				.getBytes("UTF-8")));
		reader.parse(src);
		Document doc = receiver.getDocument();
		return doc;
	}
}
