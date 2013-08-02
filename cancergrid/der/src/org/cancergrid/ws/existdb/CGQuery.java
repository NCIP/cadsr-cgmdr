/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.cancergrid.ws.existdb;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.xml.transform.OutputKeys;

import org.cancergrid.ws.existdb.Session.QueryResult;
import org.exist.EXistException;
import org.exist.dom.DocumentImpl;
import org.exist.dom.NodeProxy;
import org.exist.security.Permission;
import org.exist.security.User;
import org.exist.security.xacml.AccessContext;
import org.exist.soap.Collection;
import org.exist.soap.QueryResponse;
import org.exist.soap.QueryResponseCollection;
import org.exist.soap.QueryResponseCollections;
import org.exist.soap.QueryResponseDocument;
import org.exist.soap.QueryResponseDocuments;
import org.exist.soap.StringArray;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.util.Configuration;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.AnalyzeContextInfo;
import org.exist.xquery.PathExpr;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.parser.XQueryLexer;
import org.exist.xquery.parser.XQueryParser;
import org.exist.xquery.parser.XQueryTreeParser;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;

import antlr.collections.AST;

public class CGQuery {
	
	private BrokerPool pool;

	public CGQuery() {
		try {
			if (!BrokerPool.isConfigured())
				configure();
			pool = BrokerPool.getInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize broker pool");
		}
	}
	
	private void configure() throws Exception {
		Configuration config = new Configuration();
		BrokerPool.configure(1, 5, config);
	}

	private Session getSession(String id) throws RemoteException {
		Session session = SessionManager.getInstance().getSession(id);
		if (session == null)
			throw new RemoteException("Session is invalid or timed out");
		return session;
	}

	public java.lang.String connect(java.lang.String userId,
			java.lang.String password) throws RemoteException {
		User u = pool.getSecurityManager().getUser(userId);
		if (u == null)
			throw new RemoteException("User " + userId + " does not exist");
		if (!u.validate(password))
			throw new RemoteException("The supplied password is invalid");
		return SessionManager.getInstance().createSession(u);
	}

	public void disconnect(java.lang.String sessionId) {
		SessionManager manager = SessionManager.getInstance();
		Session session = manager.getSession(sessionId);
		if (session != null) {
			manager.disconnect(sessionId);
		}  
	}

	public org.exist.soap.Collection listCollection(java.lang.String sessionId,
			java.lang.String path) throws java.rmi.RemoteException {
		try {
			return listCollection(sessionId, XmldbURI.xmldbUriFor(path));
		} catch (URISyntaxException e) {
			throw new RemoteException("Invalid collection URI", e);
		}
	}

	public org.exist.soap.Collection listCollection(java.lang.String sessionId,	XmldbURI path) throws java.rmi.RemoteException 
	{
		Session session = getSession(sessionId);
		DBBroker broker = null;
		try {
			broker = pool.get(session.getUser());
			if (path == null)
			{
				path = XmldbURI.ROOT_COLLECTION_URI;
			}
			
			org.exist.collections.Collection collection = broker.getCollection(path);
			if (collection == null)
			{
				throw new RemoteException("collection " + path + " not found");
			}
			
			if (!collection.getPermissions().validate(session.getUser(), Permission.READ))
			{
				throw new RemoteException("permission denied");
			}
			
			Collection c = new Collection();

			// Sub-collections
			String childCollections[] = new String[collection.getChildCollectionCount()];
			int j = 0;
			for (Iterator i = collection.collectionIterator(); i.hasNext(); j++)
			{
				childCollections[j] = ((XmldbURI) i.next()).toString();
			}

			// Resources
			String[] resources = new String[collection.getDocumentCount()];
			j = 0;
			XmldbURI resource;
			for (Iterator i = collection.iterator(broker); i.hasNext(); j++) 
			{
				resource = ((DocumentImpl) i.next()).getFileURI();
				resources[j] = resource.lastSegment().toString();
			}
			c.setResources(new StringArray(resources));
			c.setCollections(new StringArray(childCollections));
			return c;
		} catch (EXistException e) {
			throw new RemoteException(e.getMessage());
		} finally {
			pool.release(broker);
		}
	}

	public QueryResponse xquery(java.lang.String sessionId, byte[] xquery) throws RemoteException 
	{
		String query;
		try {
			query = new String(xquery, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			query = new String(xquery);
		}
		return query(sessionId, query);
	}

	public QueryResponse query(java.lang.String sessionId, java.lang.String xpath) throws RemoteException 
	{
		String status = "Test";
		Session session = getSession(sessionId);
		DBBroker broker = null;
		QueryResponse resp = new QueryResponse();
        resp.setHits(0);
		try {
			broker = pool.get(session.getUser());
			XQueryContext context = new XQueryContext(broker, AccessContext.SOAP);
			checkModuleLoadPath(context);
			XQueryLexer lexer = new XQueryLexer(context, new StringReader(xpath));
			XQueryParser parser = new XQueryParser(lexer);
			XQueryTreeParser treeParser = new XQueryTreeParser(context);
			parser.xpath();
			if (parser.foundErrors()) 
			{
				throw new RemoteException(parser.getErrorMessage());
			}
			AST ast = parser.getAST();
			PathExpr expr = new PathExpr(context);
			treeParser.xpath(ast, expr);
			if (treeParser.foundErrors()) {
				throw new EXistException(treeParser.getErrorMessage());
			}
			long start = System.currentTimeMillis();
			expr.analyze(new AnalyzeContextInfo());
			Sequence seq = expr.eval(null, null);
			QueryResponseCollection[] collections = null;
            if (!seq.isEmpty() && Type.subTypeOf(seq.getItemType(), Type.NODE))
            {
                collections = collectQueryInfo(scanResults(seq));
            }
            session.addQueryResult(seq);
            resp.setCollections(new QueryResponseCollections(collections));
            resp.setHits(seq.getItemCount());
            resp.setQueryTime(System.currentTimeMillis() - start);

			expr.reset();
			context.reset();

		} catch (Exception e) {
			System.err.println(status + "\n\n" + e);
			throw new RemoteException("query execution failed: " + e.getMessage());
		} finally {
			pool.release(broker);
		}
		return resp;
	}

	private void checkModuleLoadPath(XQueryContext context) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle("exist-soap");
			String moduleLoadPath = bundle.getString("module.load.path");
			context.setModuleLoadPath(moduleLoadPath);
		} catch (NullPointerException npe) {
		} catch (MissingResourceException mre) {
		} catch (ClassCastException cce) {
		}
	}

	public java.lang.String[] retrieve(java.lang.String sessionId, int start,
			int howmany, boolean indent, boolean xinclude,
			java.lang.String highlight) throws java.rmi.RemoteException {
		Session session = getSession(sessionId);
		DBBroker broker = null;
		try {
			broker = pool.get(session.getUser());
			QueryResult queryResult = session.getQueryResult();
			if (queryResult == null)
				throw new RemoteException("result set unknown or timed out");
			Sequence seq = (Sequence) queryResult.result;
			if (start < 1 || start > seq.getItemCount())
				throw new RuntimeException("index " + start
						+ " out of bounds (" + seq.getItemCount() + ")");
			if (start + howmany > seq.getItemCount() || howmany == 0)
				howmany = seq.getItemCount() - start + 1;

			String xml[] = new String[howmany];
			Serializer serializer = broker.getSerializer();
			serializer.reset();
			serializer.setProperty(OutputKeys.INDENT, indent ? "yes" : "no");
			serializer.setProperty(EXistOutputKeys.EXPAND_XINCLUDES,
					xinclude ? "yes" : "no");
			serializer
					.setProperty(EXistOutputKeys.HIGHLIGHT_MATCHES, highlight);

			Item item;
			int j = 0;
			for (int i = --start; i < start + howmany; i++, j++) {
				item = seq.itemAt(i);
				if (item == null)
					continue;
				if (item.getType() == Type.ELEMENT) {
					NodeValue node = (NodeValue) item;
					xml[j] = serializer.serialize(node);
				} else {
					xml[j] = item.getStringValue();
				}
			}
			return xml;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage(), e);
		} finally {
			pool.release(broker);
		}
	}
	
	private TreeMap scanResults(Sequence results) throws RemoteException {
        TreeMap collections = new TreeMap();
        TreeMap documents;
        Integer hits;
        try {
			for (SequenceIterator i = results.iterate(); i.hasNext(); ) {
			    Item item = i.nextItem();
			    if(Type.subTypeOf(item.getType(), Type.NODE)) {
			        NodeValue node = (NodeValue)item;
			        if(node.getImplementationType() == NodeValue.PERSISTENT_NODE) {
			            NodeProxy p = (NodeProxy)node;
			            if ((documents = (TreeMap) collections.get(p.getDocument().getCollection().getURI())) == null) {
			                documents = new TreeMap();
			                collections.put(p.getDocument().getCollection().getURI(), documents);
			            }
			            if ((hits = (Integer) documents.get(p.getDocument().getFileURI())) == null)
			                documents.put(p.getDocument().getFileURI(), new Integer(1));
			            else
			                documents.put(p.getDocument().getFileURI(), new Integer(hits.intValue() + 1));
			        }
			    }
			}
		} catch (XPathException e) {
			throw new RemoteException(e.getMessage());
		}
        return collections;
    }
	
	private QueryResponseCollection[] collectQueryInfo(TreeMap collections) {
        QueryResponseCollection c[] = new QueryResponseCollection[collections.size()];
        QueryResponseDocument doc;
        QueryResponseDocument docs[];
        XmldbURI docId;
        int k = 0;
        int l;
        TreeMap documents;
        for (Iterator i = collections.entrySet().iterator(); i.hasNext(); k++) {
            Map.Entry entry = (Map.Entry) i.next();
            c[k] = new QueryResponseCollection();
            c[k].setCollectionName(((XmldbURI) entry.getKey()).toString());
            documents = (TreeMap) entry.getValue();
            docs = new QueryResponseDocument[documents.size()];
            c[k].setDocuments(new QueryResponseDocuments(docs));
            l = 0;
            for (Iterator j = documents.entrySet().iterator(); j.hasNext(); l++) {
                Map.Entry docEntry = (Map.Entry) j.next();
                doc = new QueryResponseDocument();
                docId = (XmldbURI) docEntry.getKey();
                //TODO: Unnecessary?
                docId = docId.lastSegment();
                doc.setDocumentName(docId.toString());
                doc.setHitCount(((Integer) docEntry.getValue()).intValue());
                docs[l] = doc;
            }
        }
        return c;
    }

}

