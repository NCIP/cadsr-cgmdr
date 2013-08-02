/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.util;

import java.io.File;
import java.io.FilenameFilter;

public class XMLFilenameFilter implements FilenameFilter {

    public XMLFilenameFilter() {
    }

    public boolean accept(File dir, String name) {
        MimeTable mimetab = MimeTable.getInstance();
        MimeType mime = mimetab.getContentTypeFor(name);
        return mime != null && mime.isXMLType();
    }
}
