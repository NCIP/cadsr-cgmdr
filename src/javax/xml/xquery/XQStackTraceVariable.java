/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package javax.xml.xquery;

import java.io.Serializable;

import javax.xml.namespace.QName;

/**
 * XQJ interfaces reconstructed from version 0.5 documentation
 */
public class XQStackTraceVariable implements Serializable {
    private QName qname;
    private String value;

    XQStackTraceVariable(QName qname, java.lang.String value) {
        this.qname = qname;
        this.value = value;
    }

    public QName getQName() {
        return qname;
    }

    public String getValue() {
        return value;
    }
}
