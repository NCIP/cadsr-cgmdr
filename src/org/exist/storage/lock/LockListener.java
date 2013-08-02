/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.storage.lock;

/**
 * Notify a listener that a lock has been released.
 */
public interface LockListener {

    void lockReleased();
}