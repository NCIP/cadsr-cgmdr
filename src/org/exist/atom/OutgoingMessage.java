/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

/*
 * OutgoingMIMEMessage.java
 *
 * Created on June 14, 2006, 11:55 PM
 *
 * (C) R. Alexander Milowski alex@milowski.com
 */

package org.exist.atom;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 *
 * @author R. Alexander Milowski
 */
public interface OutgoingMessage {
   void setStatusCode(int code);
   void setContentType(String mimeType);
   void setHeader(String key, String value);
   OutputStream getOutputStream()
      throws IOException;
   Writer getWriter()
      throws IOException;
   
   HttpServletResponse getResponse();
}
