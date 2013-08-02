/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package javax.xml.xquery;

/**
 * XQJ interfaces reconstructed from version 0.5 documentation
 */
public interface XQResultItem extends XQItem, XQItemAccessor {

    void clearWarnings();

    XQConnection getConnection() throws XQException;

    XQWarning getWarnings() throws XQException;
}
