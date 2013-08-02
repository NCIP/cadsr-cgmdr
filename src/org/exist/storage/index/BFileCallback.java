/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.storage.index;

import org.exist.storage.btree.Value;

public interface BFileCallback {

    public void info(Value key, Value value);

}
