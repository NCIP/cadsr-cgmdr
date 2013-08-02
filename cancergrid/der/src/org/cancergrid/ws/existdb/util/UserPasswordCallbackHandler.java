/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/**
 * Copyright (c) 2005-2008 CancerGrid Consortium <http://www.cancergrid.org/>
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

package org.cancergrid.ws.existdb.util;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import java.io.IOException;

import org.cancergrid.ws.existdb.CGQuery;
import org.exist.soap.QueryResponse;
import org.apache.log4j.Logger;

/**
 * A password callback handler used in Axis2 WS-Security UsernameToken communication to authenticate users against eXist own
 * user management store.
 *
 * @author <a href="mailto:Andrew.Tsui@comlab.ox.ac.uk">Andrew Tsui</a> (<a href="http://www.cancergrid.org">CancerGrid Consortium</a>)
 * @version 1.0
 */
public class UserPasswordCallbackHandler implements CallbackHandler
{

	private static Logger LOG = Logger.getLogger(UserPasswordCallbackHandler.class);

	private CGQuery query = null;
	private String userId = "guest";
	private String password = "guest";

	public UserPasswordCallbackHandler()
	{
		query = new CGQuery();
	}

	public UserPasswordCallbackHandler(String userId, String password)
	{
		query = new CGQuery();
		this.userId = userId;
		this.password = password;
	}

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException
	{
		for (int i = 0; i < callbacks.length; i++)
		{

			// When the server side need to authenticate the user
			WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
			if (pwcb.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN)
			{
				LOG.info("Is Valid User: "+isValidUser(pwcb.getIdentifer(), pwcb.getPassword()));
				if (isValidUser(pwcb.getIdentifer(), pwcb.getPassword()))
				{
					return;
				}
				else
				{
					throw new UnsupportedCallbackException(callbacks[i], "check failed");
				}
			}

		}
	}

            /**
	 * Authenticate user against eXist registered users
	 *
	 * @param user username
	 * @param pass password
	 * @return true is user is valid, otherwise false.
	 */
	private boolean isValidUser(String user, String pass)
	{
		String xq = "declare namespace xmldb=\"http://exist-db.org/xquery/xmldb\"; "
				+ "xmldb:authenticate('xmldb:exist:///db/mdr/data', '"
				+ user
				+ "', '" + pass + "')";
		return executeXQuery(xq).equals("true");
	}

            /**
	 * Utility for executing xquery through eXist
	 *
	 * @param xquery xquery to execute
	 * @return results from executing the xquery
	 */
	private String executeXQuery(String xquery)
	{
		try
		{
			String result = null;
			byte[] bin = xquery.getBytes("UTF-8");
			String sessionId = query.connect(userId, password);
			QueryResponse response = query.xquery(sessionId, bin);
			if (response.getHits() > 0)
			{
				result = query.retrieve(sessionId, 1, response.getHits(), true,
						false, "none")[0];
			}
			query.disconnect(sessionId);
			return result;
		}
		catch (Exception e)
		{
			LOG.error("executeXQuery: " + e);
			return null;
		}
	}

}
