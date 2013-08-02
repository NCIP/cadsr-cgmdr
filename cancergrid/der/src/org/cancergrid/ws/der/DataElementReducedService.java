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
 
package org.cancergrid.ws.der;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.cancergrid.schema.dataelementreduced.NewDataElementReducedDocument;
import org.cancergrid.services.dataelementreduced.CreateDataElementReducedDocument;
import org.cancergrid.services.dataelementreduced.CreateDataElementReducedResponseDocument;
import org.cancergrid.services.dataelementreduced.DataElementListSearchDocument;
import org.cancergrid.services.dataelementreduced.DataElementListSearchResponseDocument;
import org.cancergrid.services.dataelementreduced.DataElementReducedSkeletonInterface;
import org.cancergrid.services.dataelementreduced.GetDataTypesResponseDocument;
import org.cancergrid.services.dataelementreduced.GetOrganizationContactsResponseDocument;
import org.cancergrid.services.dataelementreduced.GetRegAuthResponseDocument;
import org.cancergrid.services.dataelementreduced.GetUOMResponseDocument;
import org.cancergrid.ws.existdb.CGQuery;
import org.exist.soap.QueryResponse;

/**
 * A web service that exposed operation for users to submit new data element to cgMDR. It is essentially a wrapper, which exposed stored XQuery located in
 * /db/mdr/services.
 *
 * @author <a href="mailto:Andrew.Tsui@comlab.ox.ac.uk">Andrew Tsui</a> (<a href="http://www.cancergrid.org">CancerGrid Consortium</a>)
 * @version 1.0
 */
public class DataElementReducedService implements DataElementReducedSkeletonInterface
{
	private static Logger LOG = Logger.getLogger(DataElementReducedService.class);

	private CGQuery query = null;
	private String defaultUserId = "guest";
	private String defaultPassword = "guest";

	public DataElementReducedService()
	{
		query = new CGQuery();
	}

	public DataElementReducedService(String userId, String password)
	{
		query = new CGQuery();
		this.defaultUserId = userId;
		this.defaultPassword = password;
	}

             /**
	 * Do a simple data element query 
	 *
	 * @param query a query consist of search term, start index, and number of results to return 
	 * @return list of matching data element
	 */
	@Override
	public DataElementListSearchResponseDocument dataElementListSearch(DataElementListSearchDocument query)
	{
		String xq = "import module namespace lib-search=\"http://www.cancergrid.org/xquery/library/search\" "
				+ "	at \"m-lib-search.xquery\";"
				+ "lib-search:dataElementListSearch('"
				+ query.getDataElementListSearch().getTerm()
				+ "',"
				+ query.getDataElementListSearch().getStartIndex()
				+ ","
				+ query.getDataElementListSearch().getNumResults() + ")";

		DataElementListSearchResponseDocument response = DataElementListSearchResponseDocument.Factory.newInstance();
		response.addNewDataElementListSearchResponse().setReturn(executeXQuery(xq));
		return response;
	}

            /**
	 * Create a new data element in cgMDR. 
	 *
	 * @param req details of the new data element 
	 * @return resulting data element in reduced format
	 */
	@Override
	public CreateDataElementReducedResponseDocument createDataElementReduced(CreateDataElementReducedDocument req)
	{
		try
		{
			WSUsernameTokenPrincipal ut = getUsernameToken();
			if (ut.getName().equalsIgnoreCase("guest"))
			{
				throw new Exception("No guest allowed.");
			}
			
			NewDataElementReducedDocument doc = NewDataElementReducedDocument.Factory.newInstance();
			doc.setNewDataElementReduced(req.getCreateDataElementReduced().getNewDataElementReduced());

			String xq = "import module namespace lib-der=\"http://www.cancergrid.org/xquery/library/data-element-reduced\" at \"m-lib-data-element.xquery\"; "
					+ "let $doc as element() := "
					+ doc.toString()
					+ " return lib-der:create-data-element-reduced(lib-der:filled-default-values($doc), '" + ut.getName() + "', '" + ut.getPassword() + "')";

			CreateDataElementReducedResponseDocument response = CreateDataElementReducedResponseDocument.Factory.newInstance();
			response.addNewCreateDataElementReducedResponse().setReturn(executeXQuery(xq, ut.getName(), ut.getPassword()));
			return response;
		}
		catch (Exception e)
		{
			LOG.error("createDataElementReduced(): " + e); 
			return null;
		}
	}

            /**
	 * Get data types available for filling in details of new data element 
	 *
	 * @return list of available data types
	 */
	@Override
	public GetDataTypesResponseDocument getDataTypes()
	{
		String xq = "import module namespace lib-der=\"http://www.cancergrid.org/xquery/library/data-element-reduced\""
				+ "	at \"m-lib-data-element.xquery\"; "
				+ "lib-der:get-data-types()";
		GetDataTypesResponseDocument response = GetDataTypesResponseDocument.Factory.newInstance();
		response.addNewGetDataTypesResponse().setReturn(executeXQuery(xq));
		return response;
	}

            /**
	 * Get UOM available for filling in details of new data element 
	 *
	 * @return list of available UOM
	 */
	@Override
	public GetUOMResponseDocument getUOM()
	{
		String xq = "import module namespace lib-der=\"http://www.cancergrid.org/xquery/library/data-element-reduced\""
				+ "	at \"m-lib-data-element.xquery\"; " + "lib-der:get-uom()";
		GetUOMResponseDocument response = GetUOMResponseDocument.Factory.newInstance();
		response.addNewGetUOMResponse().setReturn(executeXQuery(xq));
		return response;
	}

            /**
	 * Get organization contacts available for filling in details of new data element 
	 *
	 * @return list of available organization contacts
	 */
	@Override
	public GetOrganizationContactsResponseDocument getOrganizationContacts()
	{
		String xq = "import module namespace lib-der=\"http://www.cancergrid.org/xquery/library/data-element-reduced\""
				+ "	at \"m-lib-data-element.xquery\"; "
				+ "lib-der:get-organization-contacts()";
		GetOrganizationContactsResponseDocument response = GetOrganizationContactsResponseDocument.Factory.newInstance();
		response.addNewGetOrganizationContactsResponse().setReturn(executeXQuery(xq));
		return response;
	}

            /**
	 * Get regisration authority contacts available for filling in details of new data element 
	 *
	 * @return list of available regisration authority
	 */
	@Override
	public GetRegAuthResponseDocument getRegAuth()
	{
		String xq = "import module namespace lib-der=\"http://www.cancergrid.org/xquery/library/data-element-reduced\""
				+ "	at \"m-lib-data-element.xquery\"; "
				+ "lib-der:get-reg-auth()";
		GetRegAuthResponseDocument response = GetRegAuthResponseDocument.Factory.newInstance();
		response.addNewGetRegAuthResponse().setReturn(executeXQuery(xq));
		return response;
	}

	private String executeXQuery(String xquery)
	{
		return executeXQuery(xquery, defaultUserId, defaultPassword);
	}
	
	private String executeXQuery(String xquery, String userId, String password)
	{
		try
		{
			String result = null;
			byte[] bin = xquery.getBytes("UTF-8");
			String sessionId = query.connect(userId, password);
			QueryResponse response = query.xquery(sessionId, bin);
			if (response.getHits() > 0)
			{
				result = query.retrieve(sessionId, 1, response.getHits(), true,	false, "none")[0];
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

            /**
	 * Get usernametoken from the underlying SOAP request 
	 *
	 * @return user's priciple
	 */
	@SuppressWarnings("unchecked")
	private WSUsernameTokenPrincipal getUsernameToken()
	{
		try
		{
			org.apache.axis2.context.MessageContext msgCtx = org.apache.axis2.context.MessageContext.getCurrentMessageContext();
			Vector<WSHandlerResult> results = null;
			if ((results = (Vector<WSHandlerResult>) msgCtx.getProperty(WSHandlerConstants.RECV_RESULTS)) == null)
			{
				throw new RuntimeException("No security results!!");
			}
			else
			{
				WSSecurityEngineResult wser = null;
				for (int i = 0; i < results.size(); i++)
				{
					// Get hold of the WSHandlerResult instance
					WSHandlerResult rResult = results.elementAt(i);
					System.out.println("rResult: "+rResult.toString());
					Vector<WSSecurityEngineResult> wsSecEngineResults = (Vector<WSSecurityEngineResult>)rResult.getResults();
					System.out.println(wsSecEngineResults.size()+"");
					for (int j = 0; j < wsSecEngineResults.size(); j++)
					{
						// Get hold of the WSSecurityEngineResult instance
						wser = wsSecEngineResults.elementAt(j);
						if (wser.get(WSSecurityEngineResult.TAG_PRINCIPAL) != null)
						{
							break;
						}
					}
					if (wser.get(WSSecurityEngineResult.TAG_PRINCIPAL) != null)
					{
						break;
					}
				}
				// Extract the principal
				WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal) wser.get(WSSecurityEngineResult.TAG_PRINCIPAL);
				return principal;
			}
		}
		catch (Exception e)
		{
			LOG.error("getUsernameToken(): " + e);
			return null;
		}
	}
}
