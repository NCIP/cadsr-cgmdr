/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package javax.xml.namespace;

import java.util.Iterator;

/**
 * A local copy of the JAXP 1.3 NamespaceContext interface, included here to avoid problems
 * when running under JDK 1.4.
 */
public interface NamespaceContext {
    String getNamespaceURI(String prefix);
    String getPrefix(String namespaceURI);
    Iterator getPrefixes(String namespaceURI);
}
