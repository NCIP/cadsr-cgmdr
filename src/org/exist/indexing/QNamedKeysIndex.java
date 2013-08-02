/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.indexing;

/**
 * Indexes that store their values with an information about the QName of their nodes 
 * should implement this interface.
 * 
 * @author brihaye
 *
 */
public interface QNamedKeysIndex extends IndexWorker {

    /**
     * A key to a QName {@link java.util.List} "hint" to be used when the index scans its index entries
     */	
	public static final String QNAMES_KEY = "qnames_key";
	
}
