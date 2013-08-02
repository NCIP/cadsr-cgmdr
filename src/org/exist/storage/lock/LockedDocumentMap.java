/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 * eXist Open Source Native XML Database
 * Copyright (C) 2007 The eXist Project
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
 *  $Id: LockedDocumentMap.java 6340 2007-08-08 16:24:38Z wolfgang_m $
 */
package org.exist.storage.lock;

import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.util.hashtable.Int2ObjectHashMap;
import org.exist.collections.Collection;

import java.util.Iterator;

/**
 * This map is used by the XQuery engine to track how many read locks were
 * acquired for a document during query execution.
 */
public class LockedDocumentMap extends Int2ObjectHashMap {

    public LockedDocumentMap() {
        super(29, 1.75);
    }

    public void add(DocumentImpl doc) {
        LockedDocument entry = (LockedDocument) get(doc.getDocId());
        if (entry == null) {
            entry = new LockedDocument(doc);
            put(doc.getDocId(), entry);
        }
        entry.locksAcquired++;
    }

    public DocumentSet toDocumentSet() {
        DocumentSet docs = new DocumentSet(size());
        LockedDocument d;
        for(int idx = 0; idx < tabSize; idx++) {
	        if(values[idx] == null || values[idx] == REMOVED)
	            continue;
	        d = (LockedDocument) values[idx];
            docs.add(d.document);
        }
        return docs;
    }

    public DocumentSet getDocsByCollection(Collection collection, boolean includeSubColls, DocumentSet targetSet) {
        if (targetSet == null)
            targetSet = new DocumentSet(size());
        LockedDocument d;
        for(int idx = 0; idx < tabSize; idx++) {
	        if(values[idx] == null || values[idx] == REMOVED)
	            continue;
	        d = (LockedDocument) values[idx];
            if (d.document.getCollection().getURI().startsWith(collection.getURI()))
                targetSet.add(d.document);
        }
        return targetSet;
    }

    public void unlock() {
	    LockedDocument d;
	    Lock dlock;
        for(int idx = 0; idx < tabSize; idx++) {
	        if(values[idx] == null || values[idx] == REMOVED)
	            continue;
	        d = (LockedDocument) values[idx];
            unlockDocument(d); 
        }
	}

    public LockedDocumentMap unlockSome(DocumentSet keep) {
        LockedDocument d;
        Lock dlock;
        for(int idx = 0; idx < tabSize; idx++) {
	        if(values[idx] == null || values[idx] == REMOVED)
	            continue;
	        d = (LockedDocument) values[idx];
            if (!keep.containsKey(d.document.getDocId())) {
                values[idx] = REMOVED;
                unlockDocument(d);
            }
        }
        return this;
    }

    private void unlockDocument(LockedDocument d) {
        Lock dlock = d.document.getUpdateLock();
        dlock.release(Lock.WRITE_LOCK, d.locksAcquired);
//        for (int i = 0; i < d.locksAcquired; i++) {
//            dlock.release(Lock.READ_LOCK);
//        }
//        if (dlock.isLockedForRead(Thread.currentThread())) {
//            System.out.println("Thread is still LOCKED: " + Thread.currentThread().getName());
//        }
    }

    private static class LockedDocument {
        private DocumentImpl document;
        private int locksAcquired = 0;

        public LockedDocument(DocumentImpl document) {
            this.document = document;
        }
    }
}
