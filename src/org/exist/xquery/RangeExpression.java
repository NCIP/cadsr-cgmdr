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
 *  $Id: RangeExpression.java 6469 2007-09-02 13:58:46Z brihaye $
 */
package org.exist.xquery;

import java.util.List;

import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.value.IntegerValue;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NumericValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 * An XQuery range expression, like "1 to 10".
 * 
 * @author wolf
 */
public class RangeExpression extends PathExpr {
	
	Expression start;
	Expression end;

	/**
	 * @param context
	 */
	//TODO : RangeExpression(XQueryContext context, Expressoin start, Expression end)
	//Needs parser refactoring
	public RangeExpression(XQueryContext context) {
		super(context);
	}
	
	//TODO : remove and use the other constructor
	public void setArguments(List arguments) throws XPathException {
		start = (Expression)arguments.get(0);
		end = (Expression)arguments.get(1);        
	}
	
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
    	//TODO : static checks ?
    	/*
    	if (!Cardinality.checkCardinality(Cardinality.ZERO_OR_ONE, start.getCardinality()))
		    throw new XPathException(getASTNode(), "Invalid cardinality for 1st argument");
    	if (!Cardinality.checkCardinality(Cardinality.ZERO_OR_ONE, end.getCardinality()))
		    throw new XPathException(getASTNode(), "Invalid cardinality for 2nd argument");
    	if (start.returnsType() != Type.INTEGER)
		    throw new XPathException(getASTNode(), "Invalid type for 1st argument");
    	if (end.returnsType() != Type.INTEGER)
		    throw new XPathException(getASTNode(), "Invalid type for 2nd argument");
        */
    	inPredicate = (contextInfo.getFlags() & IN_PREDICATE) > 0;
    	contextId = contextInfo.getContextId();
    	contextInfo.setParent(this);
    	start.analyze(contextInfo);
    	end.analyze(contextInfo);
    }
    
   
    /* (non-Javadoc)
	 * @see org.exist.xquery.Expression#eval(org.exist.dom.DocumentSet, org.exist.xquery.value.Sequence, org.exist.xquery.value.Item)
	 */
	public Sequence eval(Sequence contextSequence, Item contextItem) throws XPathException {		
    	Sequence result = null;
		Sequence startSeq = start.eval(contextSequence, contextItem);
		Sequence endSeq = end.eval(contextSequence, contextItem);
		if (startSeq.isEmpty())
		    result = Sequence.EMPTY_SEQUENCE;
		else if (endSeq.isEmpty())
			result = Sequence.EMPTY_SEQUENCE;
		else if (startSeq.hasMany())
			throw new XPathException(getASTNode(), "XPTY0004: the first operand must have at most one item");
		else if (endSeq.hasMany())
			throw new XPathException(getASTNode(), "XPTY0004: the second operand must have at most one item");
        else {
        	if (context.isBackwardsCompatible()) {
	        	NumericValue valueStart;
	        	try {
	        		//Currently breaks 1e3 to 3
	        		valueStart = (NumericValue)startSeq.itemAt(0).convertTo(Type.NUMBER);
	        	} catch (XPathException e) {
					throw new XPathException(getASTNode(), "FORG0006: Required type is " + 
							Type.getTypeName(Type.INTEGER) + " but got '" + Type.getTypeName(startSeq.itemAt(0).getType()) + "(" +
							startSeq.itemAt(0).getStringValue() + ")'");
	        	}
	        	NumericValue valueEnd;
	        	try {
	        		//Currently breaks 3 to 1e3
	        		valueEnd = (NumericValue)endSeq.itemAt(0).convertTo(Type.NUMBER);
	        	} catch (XPathException e) {
					throw new XPathException(getASTNode(), "FORG0006: Required type is " + 
							Type.getTypeName(Type.INTEGER) + " but got '" + Type.getTypeName(endSeq.itemAt(0).getType()) + "(" +
							endSeq.itemAt(0).getStringValue() + ")'");
	        	}
	        	//Implied by previous conversion
	        	if (valueStart.hasFractionalPart()) {
					throw new XPathException(getASTNode(), "FORG0006: Required type is " + 
							Type.getTypeName(Type.INTEGER) + " but got '" + Type.getTypeName(startSeq.itemAt(0).getType()) + "(" +
							startSeq.itemAt(0).getStringValue() + ")'");
				}
	        	//Implied by previous conversion
	        	if (valueEnd.hasFractionalPart()) {
					throw new XPathException(getASTNode(), "FORG0006: Required type is " + 
							Type.getTypeName(Type.INTEGER) + " but got '" + Type.getTypeName(endSeq.itemAt(0).getType()) + "(" +
							startSeq.itemAt(0).getStringValue() + ")'");
	        	}        	
	        	result = new ValueSequence();
				for(long i = ((IntegerValue)valueStart.convertTo(Type.INTEGER)).getLong(); 
					i <= ((IntegerValue)valueEnd.convertTo(Type.INTEGER)).getLong(); i++) {
					result.add(new IntegerValue(i));
				}
	        } else {
	        	//Quite unusual test : we accept integers but no other *typed* type 
	        	if (!Type.subTypeOf(startSeq.itemAt(0).atomize().getType(), Type.INTEGER) &&
	        		!Type.subTypeOf(startSeq.itemAt(0).atomize().getType(), Type.UNTYPED_ATOMIC))
					throw new XPathException(getASTNode(), "FORG0006: Required type is " + 
							Type.getTypeName(Type.INTEGER) + " but got '" + Type.getTypeName(startSeq.itemAt(0).getType()) + "(" +
							startSeq.itemAt(0).getStringValue() + ")'");
	        	//Quite unusual test : we accept integers but no other *typed* type 
	        	if (!Type.subTypeOf(endSeq.itemAt(0).atomize().getType(), Type.INTEGER) &&
	        		!Type.subTypeOf(endSeq.itemAt(0).atomize().getType(), Type.UNTYPED_ATOMIC))
					throw new XPathException(getASTNode(), "FORG0006: Required type is " + 
							Type.getTypeName(Type.INTEGER) + " but got '" + Type.getTypeName(endSeq.itemAt(0).getType()) + "(" +
							endSeq.itemAt(0).getStringValue() + ")'");
	        	IntegerValue valueStart = (IntegerValue)startSeq.itemAt(0).convertTo(Type.INTEGER);
	        	IntegerValue valueEnd = (IntegerValue)endSeq.itemAt(0).convertTo(Type.INTEGER);
	       		result = new ValueSequence();
				for(long i = valueStart.getLong();	i <= valueEnd.getLong(); i++) {
					result.add(new IntegerValue(i));
				}        	
	        }
        }
		return result;
	}

    public void dump(ExpressionDumper dumper) {
        dumper.display(start);
        dumper.display(" to ");
        dumper.display(end);
    }
    
    public int returnsType() {
        return Type.INTEGER;
    }    
	
}
