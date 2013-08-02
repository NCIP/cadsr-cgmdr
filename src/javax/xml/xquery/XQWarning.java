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
public class XQWarning extends XQException {

    public XQWarning(String message) {
            super(message);
            }

    public XQWarning(java.lang.String message, java.lang.Throwable cause, java.lang.String vendorcode, XQWarning nextWarning) {
        super(message, cause, vendorcode, nextWarning);

    }
}
