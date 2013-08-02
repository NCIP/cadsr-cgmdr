/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-07 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: SearchResourceResolver.java 6810 2007-10-28 20:26:05Z dizzzz $
 */

package org.exist.validation.resolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.exist.security.SecurityManager;
import org.exist.security.User;
import org.exist.storage.BrokerPool;
import org.exist.validation.internal.DatabaseResources;
import org.xml.sax.InputSource;

/**
 *  Resolve a resource by searching in database. Schema's are queried
 * directly, DTD are searched in catalog files.
 *
 * @author Dannes Wessels (dizzzz@exist-db.org)
 */
public class SearchResourceResolver implements XMLEntityResolver {
    private final static Logger LOG = Logger.getLogger(SearchResourceResolver.class);
    
    private String collection=null;
    private BrokerPool brokerPool = null;
    
    /** Creates a new instance of StoredResourceResolver */
    public SearchResourceResolver(String collectionPath, BrokerPool pool) {
        LOG.debug("Specified collectionPath="+collectionPath);
        collection=collectionPath;
        brokerPool=pool;
    }
    
    /**
     *
     */
    public XMLInputSource resolveEntity(XMLResourceIdentifier xri) throws XNIException, IOException {
        
        if(xri.getExpandedSystemId()==null && xri.getLiteralSystemId()==null && 
           xri.getNamespace()==null && xri.getPublicId()==null){
            
            // quick fail
            return null;
        }
        LOG.debug("Resolving XMLResourceIdentifier: "+getXriDetails(xri));


        String resourcePath = null;
        
        DatabaseResources databaseResources = new DatabaseResources(brokerPool);
        
        User user = brokerPool.getSecurityManager().getUser(SecurityManager.GUEST_USER);
        
        if( xri.getNamespace() !=null ){
            
            // XML Schema search
            LOG.debug("Searching namespace '"+xri.getNamespace()+"' in database from "+collection+"...");
            
            resourcePath = databaseResources.findXSD(collection, xri.getNamespace(), user);
            
            // DIZZZ: set systemid?
            
        } else if ( xri.getPublicId() !=null ){
            
            // Catalog search
            LOG.debug("Searching publicId '"+xri.getPublicId()+"' in catalogs in database from "+collection+"...");
            
            String catalogPath = databaseResources.findCatalogWithDTD(collection, xri.getPublicId(), user);
            LOG.info("Found publicId in catalog '"+catalogPath+"'");
            if(catalogPath!=null && catalogPath.startsWith("/")){
                catalogPath="xmldb:exist://"+catalogPath;
            }
            
            eXistXMLCatalogResolver resolver = new eXistXMLCatalogResolver();
            resolver.setCatalogList(new String[]{catalogPath});
            try {
                InputSource source = resolver.resolveEntity(xri.getPublicId(), "");
                if(source!=null){
                    resourcePath=source.getSystemId();
                }
            } catch (Exception ex) {
                LOG.debug(ex);
                ex.printStackTrace();
            }
            
            // set systemid?
            
        } else {
            // Fast escape; no logging, otherwise validation is slow!
            return null;
        }
        
        // Another escape route
        if(resourcePath==null){
            LOG.debug("resourcePath=null");
            return null;
        }
        
        if(resourcePath.startsWith("/")){
            resourcePath="xmldb:exist://"+resourcePath;
        }
        
        LOG.debug("resourcePath='"+resourcePath+"'");
        
        InputStream is = new URL(resourcePath).openStream();
        
        XMLInputSource xis = new XMLInputSource(xri.getPublicId(),
            xri.getExpandedSystemId(), xri.getBaseSystemId(), is, "UTF-8");

        LOG.debug( "XMLInputSource: "+getXisDetails(xis) );
        
        return xis;
    }
    
    private String getXriDetails(XMLResourceIdentifier xrid){
        StringBuffer sb = new StringBuffer();
        sb.append("PublicId='").append(xrid.getPublicId()).append("' ");
        sb.append("BaseSystemId='").append(xrid.getBaseSystemId()).append("' ");
        sb.append("ExpandedSystemId='").append(xrid.getExpandedSystemId()).append("' ");
        sb.append("LiteralSystemId='").append(xrid.getLiteralSystemId()).append("' ");
        sb.append("Namespace='").append(xrid.getNamespace()).append("' ");
        return sb.toString();
    }

    private String getXisDetails(XMLInputSource xis){
        StringBuffer sb = new StringBuffer();
        sb.append("PublicId='").append(xis.getPublicId()).append("' ");
        sb.append("SystemId='").append(xis.getSystemId()).append("' ");
        sb.append("BaseSystemId='").append(xis.getBaseSystemId()).append("' ");
        sb.append("Encoding='").append(xis.getEncoding()).append("' ");
        return sb.toString();
    }
    
}
