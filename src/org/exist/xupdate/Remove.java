/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database Copyright (C) 2001-04 Wolfgang M. Meier
 * wolfgang@exist-db.org http://exist.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 * 
 * $Id: Remove.java 7550 2008-03-26 10:51:39Z wolfgang_m $
 */
package org.exist.xupdate;

import org.exist.EXistException;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.dom.NodeImpl;
import org.exist.dom.StoredNode;
import org.exist.security.Permission;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.DBBroker;
import org.exist.storage.NotificationService;
import org.exist.storage.UpdateListener;
import org.exist.storage.txn.Txn;
import org.exist.util.LockException;
import org.exist.xquery.XPathException;
import org.w3c.dom.Node;

import java.util.Map;

/**
 * Implements an XUpdate remove operation.
 * 
 * @author Wolfgang Meier
 */
public class Remove extends Modification {

    /**
     * Constructor for Remove.
     * 
     * 
     * @param broker 
     * @param docs 
     * @param namespaces 
     * @param variables 
     * @param selectStmt 
     */
	public Remove(DBBroker broker, DocumentSet docs, String selectStmt,
			Map namespaces, Map variables) {
		super(broker, docs, selectStmt, namespaces, variables);
	}

	/**
	 * @see org.exist.xupdate.Modification#process(org.exist.storage.txn.Txn)
	 */
	public long process(Txn transaction) throws PermissionDeniedException,
			LockException, EXistException, XPathException {
		try {
			StoredNode[] ql = selectAndLock(transaction);
			IndexListener listener = new IndexListener(ql);
			NotificationService notifier = broker.getBrokerPool()
					.getNotificationService();
			NodeImpl parent;
			for (int i = 0; i < ql.length; i++) {
				StoredNode node = ql[i];
                DocumentImpl doc = (DocumentImpl)node.getOwnerDocument();
				if (!doc.getPermissions().validate(broker.getUser(),
						Permission.UPDATE))
					throw new PermissionDeniedException(
							"permission to update document denied");
				doc.getMetadata().setIndexListener(listener);
				parent = (NodeImpl) node.getParentNode();
                if (parent == null || parent.getNodeType() != Node.ELEMENT_NODE) {
					throw new EXistException(
							"you cannot remove the document element. Use update "
									+ "instead");
				} else
					parent.removeChild(transaction, node);
				doc.getMetadata().clearIndexListener();
				doc.getMetadata().setLastModified(System.currentTimeMillis());
				modifiedDocuments.add(doc);
				broker.storeXMLResource(transaction, doc);
				notifier.notifyUpdate(doc, UpdateListener.UPDATE);
            }
			checkFragmentation(transaction, modifiedDocuments);
			return ql.length;
		} finally {
			unlockDocuments(transaction);
		}
	}

	/**
	 * @see org.exist.xupdate.Modification#getName()
	 */
	public String getName() {
		return "remove";
	}

}