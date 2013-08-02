/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.jboss;

import org.jboss.system.ServiceMBean;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;

/**
 * This are the managed operations and attributes for the XmlDb service
 *
 * @author Per Nyfelt
 */
public interface XmlDbServiceMBean extends ServiceMBean {

    public String getDriver();

    public void setDriver(String driver);

    public String getBaseCollectionURI();

    public void setBaseCollectionURI(String baseCollectionURI);

    public Collection getBaseCollection() throws XMLDBException;

}
