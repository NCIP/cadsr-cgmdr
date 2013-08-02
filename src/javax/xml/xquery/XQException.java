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
public class XQException extends Exception {

    private String vendorCode;
    private XQException nextException;

    public XQException(String message) {
        super(message);
    }

    public XQException(String message, Throwable cause, String vendorCode, XQException nextException) {
        super(message, cause);
        this.vendorCode = vendorCode;
        this.nextException = nextException;
    }

    XQException getNextException() {
        return nextException;
    }

    java.lang.String getVendorCode() {
        return vendorCode;
    }

    void setNextException(XQException next) {
        nextException = next;
    }
}
