/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 *  eXist Native XML Database
 *  Copyright (C) 2000-03,  Wolfgang M. Meier (wolfgang@exist-db.org)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * $Id: Function.java 6958 2007-11-21 07:21:43Z brihaye $
 */
package org.exist.xquery;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;

import org.exist.dom.QName;
import org.exist.xquery.parser.XQueryAST;
import org.exist.xquery.util.Error;
import org.exist.xquery.util.ExpressionDumper;
import org.exist.xquery.util.Messages;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.Type;

/**
 * Abstract base class for all built-in and user-defined functions.
 * 
 * Built-in functions just extend this class. A new function instance
 * will be created for each function call. Subclasses <b>have</b> to
 * provide a function signature to the constructor.
 * 
 * User-defined functions extend class {@link org.exist.xquery.UserDefinedFunction},
 * which is again a subclass of Function. They will not be called directly, but through a
 * {@link org.exist.xquery.FunctionCall} object, which checks the type and cardinality of
 * all arguments and takes care that the current execution context is saved properly.
 * 
 * @author wolf
 */
public abstract class Function extends PathExpr {
	// Declare it in Namespaces instead? /ljo
	public final static String BUILTIN_FUNCTION_NS =
		"http://www.w3.org/2005/xpath-functions";
	
	// The signature of the function.	
	protected FunctionSignature mySignature;
	
	// The parent expression from which this function is called.
	private Expression parent;
	
	private XQueryAST astNode = null;
	
	/**
	 * Internal constructor. Subclasses should <b>always</b> call this and
	 * pass the current context and their function signature.
	 * 
	 * @param context
	 * @param signature
	 */
	protected Function(XQueryContext context, FunctionSignature signature) {
		super(context);
		this.mySignature = signature;
	}

	protected Function(XQueryContext context) {
		super(context);
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.PathExpr#returnsType()
	 */
	public int returnsType() {
		if(mySignature == null)
			return Type.ITEM;		// Type is not known yet
		if(mySignature.getReturnType() == null)
			throw new IllegalArgumentException("Return type for function " + mySignature.getName() +
					" is not defined");
		return mySignature.getReturnType().getPrimaryType();
	}

	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#getCardinality()
	 */
	public int getCardinality() {
		if(mySignature.getReturnType() == null)
			throw new IllegalArgumentException("Return type for function " + mySignature.getName() +
					" is not defined");
		return mySignature.getReturnType().getCardinality();
	}
	
	/**
	 * Create a built-in function from the specified class.
	 * @return the created function or null if the class could not be initialized.
	 */
	public static Function createFunction(
		XQueryContext context,
		XQueryAST ast,
		FunctionDef def) throws XPathException {
		Class fclass = def.getImplementingClass();
		if (def == null || fclass == null)
			throw new XPathException(ast, "Class for function is null");
		try {
			Object initArgs[] = { context };
			Class constructorArgs[] = { XQueryContext.class }; 
			Constructor construct = null;
			try {
				construct = fclass.getConstructor(constructorArgs);
			} catch(NoSuchMethodException e) {
			}
			// not found: check if the constructor takes two arguments
			if (construct == null) {
				constructorArgs = new Class[2];
				constructorArgs[0] = XQueryContext.class;
				constructorArgs[1] = FunctionSignature.class;
				construct = fclass.getConstructor(constructorArgs);
				if(construct == null)
					throw new XPathException(ast, "Constructor not found");
				initArgs = new Object[2];
				initArgs[0] = context;
				initArgs[1] = def.getSignature();
			}
			Object obj = construct.newInstance(initArgs);
			if (obj instanceof Function) {
				((Function)obj).setASTNode(ast);
				return (Function) obj;
			} else
				throw new XPathException(ast, "Function object does not implement interface function");
		} catch (Exception e) {
			LOG.debug(e.getMessage(), e);
			throw new XPathException(ast, "Function implementation class " + fclass.getName() + " not found");
		}
	}

	/**
	 * Set the parent expression of this function, i.e. the
	 * expression from which the function is called.
	 * 
	 * @param parent
	 */
	public void setParent(Expression parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the expression from which this function
	 * gets called.
         */
	public Expression getParent() {
		return parent;
	}
	
	/**
	 * Set the (static) arguments for this function from a list of expressions.
	 * 
	 * This will also check the type and cardinality of the
	 * passed argument expressions.
	 * 
	 * @param arguments
	 * @throws XPathException
	 */
	public void setArguments(List arguments) throws XPathException {
		if ((!mySignature.isOverloaded())
			&& arguments.size() != mySignature.getArgumentCount())
			throw new XPathException(getASTNode(),
				"number of arguments to function "
					+ getName()
					+ " doesn't match function signature (expected "
					+ mySignature.getArgumentCount()
					+ ", got "
					+ arguments.size()
					+ ')');
        steps = arguments;
	}

    /**
     * @throws XPathException
     */
    protected void checkArguments() throws XPathException {
        SequenceType[] argumentTypes = mySignature.getArgumentTypes();
        Expression next;
		SequenceType argType = null;
		for (int i = 0; i < getArgumentCount(); i++) {
			if (argumentTypes != null && i < argumentTypes.length)
				argType = argumentTypes[i];
			next = checkArgument(getArgument(i), argType, i + 1);
            steps.set(i, next);
		}
    }

	/**
	 * Statically check an argument against the sequence type specified in
	 * the signature.
	 * 
	 * @param expr
	 * @param type
	 * @return The passed expression
	 * @throws XPathException
	 */
	protected Expression checkArgument(Expression expr, SequenceType type, int argPosition)
		throws XPathException {
		if (type == null)
			return expr;
		// check cardinality if expected cardinality is not zero or more
		boolean cardinalityMatches =
			type.getCardinality() == Cardinality.ZERO_OR_MORE;
		if (!cardinalityMatches) {
			cardinalityMatches =
				(expr.getCardinality() | type.getCardinality())
					== type.getCardinality();
			if (!cardinalityMatches) {
				if(expr.getCardinality() == Cardinality.ZERO
				&& (type.getCardinality() & Cardinality.ZERO) == 0)
				    throw new XPathException(getASTNode(), 
				            Messages.getMessage(Error.FUNC_EMPTY_SEQ_DISALLOWED, 
				                    new Integer(argPosition), ExpressionDumper.dump(expr)));
			}
		}
		expr =
			new DynamicCardinalityCheck(
				context,
				type.getCardinality(),
				expr, new Error(Error.FUNC_PARAM_CARDINALITY, String.valueOf(argPosition), mySignature));

		// check return type if both types are not Type.ITEM
		int returnType = expr.returnsType();
        if (returnType == Type.ANY_TYPE || returnType == Type.EMPTY)
            returnType = Type.ITEM;
        
		boolean typeMatches = type.getPrimaryType() == Type.ITEM;
		typeMatches = Type.subTypeOf(returnType, type.getPrimaryType());

		if (typeMatches && cardinalityMatches) {
			if(type.getNodeName() != null)
				expr = new DynamicNameCheck(context, 
						new NameTest(type.getPrimaryType(), type.getNodeName()), expr);
			return expr;
		}
	
		//Loose argument check : we may move this, or a part hereof, to UntypedValueCheck
		if (context.isBackwardsCompatible()) {
			if (Type.subTypeOf(type.getPrimaryType(), Type.STRING)) {
				if (!Type.subTypeOf(returnType, Type.ATOMIC)) {
					expr = new Atomize(context, expr);
					returnType = Type.ATOMIC;
				}
				expr = new AtomicToString(context, expr);
				returnType = Type.STRING;
			} else if (type.getPrimaryType() == Type.NUMBER
					|| Type.subTypeOf(type.getPrimaryType(), Type.DOUBLE)) {
				if (!Type.subTypeOf(returnType, Type.ATOMIC)) {
					expr = new Atomize(context, expr);
					returnType = Type.ATOMIC;
				}
				expr =
					new UntypedValueCheck(context, type.getPrimaryType(), expr, 
                            new Error(Error.FUNC_PARAM_TYPE, String.valueOf(argPosition), mySignature));
				returnType = type.getPrimaryType();
			}
			// if the required type is an atomic type, convert the argument to an atomic 
			if (Type.subTypeOf(type.getPrimaryType(), Type.ATOMIC)) {
				if(!Type.subTypeOf(returnType, Type.ATOMIC))
					expr = new Atomize(context, expr);
				if (!(type.getPrimaryType() == Type.ATOMIC))
					expr =
						new UntypedValueCheck(context, type.getPrimaryType(), expr, 
	                            new Error(Error.FUNC_PARAM_TYPE, String.valueOf(argPosition), mySignature));
				returnType = expr.returnsType();
			}
		//Strict argument check : we may move this, or a part hereof, to UntypedValueCheck
		} else {
			// if the required type is an atomic type, convert the argument to an atomic 
			if (Type.subTypeOf(type.getPrimaryType(), Type.ATOMIC)) {
				if(!Type.subTypeOf(returnType, Type.ATOMIC))
					expr = new Atomize(context, expr);
				//if (!(type.getPrimaryType() == Type.ATOMIC))
					expr =
						new UntypedValueCheck(context, type.getPrimaryType(), expr, 
	                            new Error(Error.FUNC_PARAM_TYPE, String.valueOf(argPosition), mySignature));
				returnType = expr.returnsType();
			}
		}

		if (returnType != Type.ITEM && !Type.subTypeOf(returnType, type.getPrimaryType())) {
			if (!(Type.subTypeOf(type.getPrimaryType(), returnType) ||
					//because () is seen as a node					
					(type.getPrimaryType() == Type.EMPTY && returnType == Type.NODE))) {
                LOG.debug(ExpressionDumper.dump(expr));
                throw new XPathException(getASTNode(),
                        Messages.getMessage(Error.FUNC_PARAM_TYPE_STATIC, 
                                String.valueOf(argPosition), mySignature,
                                type.toString(), Type.getTypeName(returnType)));
            }
		}
		if (!typeMatches) {
			if(type.getNodeName() != null)
				expr = new DynamicNameCheck(context, 
						new NameTest(type.getPrimaryType(), type.getNodeName()), expr);
			else
				expr = new DynamicTypeCheck(context, type.getPrimaryType(), expr);
		}
		return expr;
	}

	/* (non-Javadoc)
     * @see org.exist.xquery.PathExpr#analyze(org.exist.xquery.Expression)
     */
    public void analyze(AnalyzeContextInfo contextInfo) throws XPathException {
        // statically check the argument list
        checkArguments();
        // call analyze for each argument
    	inPredicate = (contextInfo.getFlags() & IN_PREDICATE) > 0;
    	contextId = contextInfo.getContextId();
    	contextInfo.setParent(this);
        for(int i = 0; i < getArgumentCount(); i++) {
            getArgument(i).analyze(contextInfo);
        }
    }
    
	public abstract Sequence eval(
		Sequence contextSequence,
		Item contextItem)
		throws XPathException;

	public Sequence[] getArguments(Sequence contextSequence, Item contextItem) throws XPathException {
		if(contextItem != null)
			contextSequence = contextItem.toSequence();
		final int argCount = getArgumentCount();
		Sequence[] args = new Sequence[argCount];
		for(int i = 0; i < argCount; i++) {
			args[i] = getArgument(i).eval(contextSequence, contextItem);
		}
		return args;
	}
	
	/**
	 * Get an argument expression by its position in the
	 * argument list.
	 * 
	 * @param pos
	 */
	public Expression getArgument(int pos) {
		return getExpression(pos);
	}

	/**
	 * Get the number of arguments passed to this function.
	 * 
	 * @return number of arguments
	 */
	public int getArgumentCount() {
		return steps.size();
	}

	public void setPrimaryAxis(int axis) {
	}
	
	/**
	 * Return the name of this function.
	 * 
	 * @return name of this function
	 */
	public QName getName() {
		return mySignature.getName();
	}

	/**
	 * Get the signature of this function.
	 * 
	 * @return signature of this function
	 */
	public FunctionSignature getSignature() {
		return mySignature;
	}

	public boolean isCalledAs(String localName) {
		return localName.equals(mySignature.getName().getLocalName());
	}
	
	/* (non-Javadoc)
	 * @see org.exist.xquery.AbstractExpression#getDependencies()
	 */
	public int getDependencies() {
		return Dependency.CONTEXT_ITEM | Dependency.CONTEXT_SET;
	}
	
	/* (non-Javadoc)
     * @see org.exist.xquery.PathExpr#dump(org.exist.xquery.util.ExpressionDumper)
     */
    public void dump(ExpressionDumper dumper) {
        dumper.display(getName());
        dumper.display('(');
        boolean moreThanOne = false;
        for (Iterator i = steps.iterator(); i.hasNext();) {
			Expression e = (Expression) i.next();
			if (moreThanOne) dumper.display(", ");
			moreThanOne = true;			
			e.dump(dumper);
        }
        dumper.display(')');
    }
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	result.append(getName());
    	result.append('(');
    	boolean moreThanOne = false;
        for (Iterator i = steps.iterator(); i.hasNext();) {
			Expression e = (Expression) i.next();
			if (moreThanOne) result.append(", ");
			moreThanOne = true;
			result.append(e.toString());				
        }
        result.append(')');   
        return result.toString();
    }
    
	public void setASTNode(XQueryAST ast) {
		this.astNode = ast;
	}
	
	public XQueryAST getASTNode() {
		return astNode;
	}

    public void accept(ExpressionVisitor visitor) {
        visitor.visitBuiltinFunction(this);
    }
}
