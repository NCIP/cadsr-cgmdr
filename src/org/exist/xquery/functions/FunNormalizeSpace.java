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
 *  $Id: FunNormalizeSpace.java 6387 2007-08-20 07:21:09Z ellefj $
 */
package org.exist.xquery.functions;

import java.util.StringTokenizer;

import org.exist.dom.QName;
import org.exist.xquery.Cardinality;
import org.exist.xquery.Dependency;
import org.exist.xquery.Function;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;

/**
 * xpath-library function: string(object)
 *
 */
public class FunNormalizeSpace extends Function {
	
	public final static FunctionSignature signatures[] = {
			new FunctionSignature(
				new QName("normalize-space", Function.BUILTIN_FUNCTION_NS),
				"Returns the value of the context item with whitespace normalized by stripping leading and trailing whitespace and replacing sequences of one or more whitespace character with a single space.",
				new SequenceType[0],
				new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
			),
			new FunctionSignature(
				new QName("normalize-space", Function.BUILTIN_FUNCTION_NS),
				"Returns the value of $a with whitespace normalized by stripping " + 
				"leading and trailing whitespace and replacing sequences of one " +
				"or more whitespace character with a single space." +
				"If the value of $a is the empty sequence, returns the " +
				"zero-length string. If no argument is supplied  $a defaults " +
				"to the string value of the context item.",
				new SequenceType[] { new SequenceType(Type.STRING, Cardinality.ZERO_OR_ONE) },
				new SequenceType(Type.STRING, Cardinality.EXACTLY_ONE)
			)
	};
				
	public FunNormalizeSpace(XQueryContext context, FunctionSignature signature) {
		super(context, signature);
	}

	public int returnsType() {
		return Type.STRING;
	}
		
	public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {
        if (context.getProfiler().isEnabled()) {
            context.getProfiler().start(this);       
            context.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
            if (contextItem != null)
                context.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT ITEM", contextItem.toSequence());
        }
        
		if(contextItem != null)
			contextSequence = contextItem.toSequence();		
		
		String value = null;
		if (getSignature().getArgumentCount() == 0) {
			if (contextSequence == null)
				throw new XPathException(getASTNode(), "err:XPDY0002: Undefined context item");			
			value = !contextSequence.isEmpty() ? contextSequence.itemAt(0).getStringValue() : "";
		} else {
			Sequence seq = getArgument(0).eval(contextSequence);
			if (seq == null)
				throw new XPathException(getASTNode(), "err:XPDY0002: Undefined context item");			
			if (!seq.isEmpty())
                value = seq.getStringValue();
		}
        
        Sequence result;
        if (value == null)
            result = StringValue.EMPTY_STRING;
        else {            
    		StringBuffer buf = new StringBuffer();
    		if (value.length() > 0) {
    			StringTokenizer tok = new StringTokenizer(value);
    			while (tok.hasMoreTokens()) {
                    buf.append(tok.nextToken());
    				if (tok.hasMoreTokens()) buf.append(' ');
    			}
    		}
            result = new StringValue(buf.toString());
        }
        
        if (context.getProfiler().isEnabled()) 
            context.getProfiler().end(this, "", result); 
        
        return result;
	}
}
