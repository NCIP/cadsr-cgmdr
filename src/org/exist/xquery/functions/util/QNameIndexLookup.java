/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Team
 *
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
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id: QNameIndexLookup.java 6320 2007-08-01 18:01:06Z ellefj $
 */
package org.exist.xquery.functions.util;

import java.util.List;

import org.exist.dom.NodeSet;
import org.exist.dom.QName;
import org.exist.storage.Indexable;
import org.exist.storage.NativeValueIndex;
import org.exist.xquery.AnalyzeContextInfo;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Constants;
import org.exist.xquery.Dependency;
import org.exist.xquery.DynamicCardinalityCheck;
import org.exist.xquery.Expression;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.RootNode;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.util.Error;
import org.exist.xquery.util.Messages;
import org.exist.xquery.value.AtomicValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.QNameValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

/**
 * @author J.M. Vanel
 */
public class QNameIndexLookup extends Function {

	public final static FunctionSignature signature = new FunctionSignature(
			new QName("qname-index-lookup", UtilModule.NAMESPACE_URI, UtilModule.PREFIX),
			"Can be used to query existing qname indexes defined on a set of nodes. " +
            "The qname is specified in the first argument. " +
            "The second argument specifies a comparison value. ",
			new SequenceType[] {
					new SequenceType(Type.QNAME, Cardinality.EXACTLY_ONE),
					new SequenceType(Type.ATOMIC, Cardinality.EXACTLY_ONE) },
			new SequenceType(Type.NODE, Cardinality.ZERO_OR_MORE));

	public QNameIndexLookup(XQueryContext context) {
		super(context, signature);
	}

    
    /**
     * Overwritten: function can process the whole context sequence at once.
     * 
     * @see org.exist.xquery.Expression#getDependencies()
     */
    public int getDependencies() {
        return Dependency.CONTEXT_SET;
    }
    
    /**
     * Overwritten to disable automatic type checks. We check manually.
     * 
     * @see org.exist.xquery.Function#setArguments(java.util.List)
     */
    public void setArguments(List arguments) throws XPathException {
        // wrap arguments into a cardinality check, so an error will be generated if
        // one of the arguments returns an empty sequence
        Expression arg = (Expression) arguments.get(0);
        arg = new DynamicCardinalityCheck(context, Cardinality.ONE_OR_MORE, arg,
                new Error(Error.FUNC_PARAM_CARDINALITY, "1", mySignature));
        steps.add(arg);
        
        arg = (Expression) arguments.get(1);
        arg = new DynamicCardinalityCheck(context, Cardinality.ONE_OR_MORE, arg,
                new Error(Error.FUNC_PARAM_CARDINALITY, "2", mySignature));
        steps.add(arg);
    }
    
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
    	contextInfo.setParent(this);
        // call analyze for each argument
        inPredicate = (contextInfo.getFlags() & IN_PREDICATE) > 0;
        for(int i = 0; i < getArgumentCount(); i++) {
            getArgument(i).analyze(contextInfo);
        }
    }
    
    public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (contextSequence == null || contextSequence.isEmpty()) {
            // if the context sequence is empty, we create a default context 
            RootNode rootNode = new RootNode(context);
            contextSequence = rootNode.eval(null, null);
        }
        Sequence[] args = getArguments(null, null);
        
        Item item = args[0].itemAt(0);
        QNameValue qval;
        try {
            // attempt to convert the first argument to a QName
            qval = (QNameValue) item.convertTo(Type.QNAME);
        } catch (XPathException e) {
            // wrong type: generate a diagnostic error
            throw new XPathException(getASTNode(),
                    Messages.formatMessage(Error.FUNC_PARAM_TYPE, 
                            new Object[] { "1", mySignature.toString(), null,
                            Type.getTypeName(Type.QNAME), Type.getTypeName(item.getType()) }
                    ));
        }
        QName qname = qval.getQName();
        
        AtomicValue comparisonCriterium = args[1].itemAt(0).atomize();
        
        Sequence result = Sequence.EMPTY_SEQUENCE;

        if (comparisonCriterium instanceof Indexable) {
            NativeValueIndex valueIndex = context.getBroker().getValueIndex();
            result =
                valueIndex.find(Constants.EQ, contextSequence.getDocumentSet(), null, NodeSet.ANCESTOR,
            qname, comparisonCriterium);
        } else {
            String message = "The comparison criterium must be an Indexable: " +
            	"boolean, numeric, string; instead your criterium has type " +
            	Type.getTypeName(comparisonCriterium.getType());
        	throw new XPathException(getASTNode(), message);
        }
        return result;
    }
}