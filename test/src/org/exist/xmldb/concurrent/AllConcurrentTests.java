/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
*  eXist Open Source Native XML Database
*  Copyright (C) 2001-04 Wolfgang M. Meier (wolfgang@exist-db.org) 
*  and others (see http://exist-db.org)
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
*  $Id: AllConcurrentTests.java 4832 2006-11-18 11:57:39Z dizzzz $
*/
package org.exist.xmldb.concurrent;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class AllConcurrentTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test suite for org.exist.xmldb.concurrent");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(ConcurrentResourceTest.class));
		suite.addTest(new TestSuite(ConcurrentAttrUpdateTest.class));
		suite.addTest(new TestSuite(ConcurrentXUpdateTest.class));
		suite.addTest(new TestSuite(ConcurrentQueryTest.class));
		suite.addTest(new TestSuite(ComplexUpdateTest.class));
        suite.addTest(new TestSuite(ValueIndexUpdateTest.class));
		//$JUnit-END$
		return suite;
	}
	
	public static void main(String[] args) {
		TestRunner.run(suite());
	}
}