/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.fluent;

class StaleMarker {
	
	private boolean stale;

	synchronized void mark() {
		stale = true;
	}
	
	synchronized void check() {
		if (stale) throw new DatabaseException("stale reference to database object");
	}
	
	void track(String path) {
		Database.trackStale(path, this);
	}
	
}
