/*L
 * Copyright Oracle Inc
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/cadsr-cgmdr/LICENSE.txt for details.
 */

package org.exist.scheduler;

import java.util.Map;

import org.exist.storage.BrokerPool;

public class TestJob extends UserJavaJob {

    private String jobName = this.getClass().getName();
    
	public void execute(BrokerPool brokerpool, Map params) throws JobException {
		
		System.out.println("****** TEST JOB EXECUTED ******");

	}

	public String getName() {
		return jobName;
	}

    public void setName(String name) {
        this.jobName = name;
    }
}
