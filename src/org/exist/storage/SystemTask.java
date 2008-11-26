/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2001-04 The eXist Team
 *
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
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *  
 *  $Id: SystemTask.java 5693 2007-04-17 19:45:31Z ellefj $
 */
package org.exist.storage;

import java.util.Properties;

import org.exist.EXistException;
import org.exist.util.Configuration;

/**
 * Interface to be implemented by tasks used for system
 * maintenance. System tasks require the database to be in
 * a consistent state. All database operations will be stopped 
 * until the {@link #execute(DBBroker)} method returned
 * or throws an exception. Any exception will be caught and a warning
 * written to the log.
 * 
 * A task can be scheduled for execution 
 * via {@link BrokerPool#triggerSystemTask(SystemTask)}
 * 
 * @author wolf
 */
public interface SystemTask {

    void configure(Configuration config, Properties properties) throws EXistException;
    
	/**
	 * Execute this task.
	 * 
	 * @param broker a DBBroker object that can be used
	 * 
	 * @throws EXistException
	 */
	void execute(DBBroker broker) throws EXistException;
}