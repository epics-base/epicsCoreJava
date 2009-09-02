/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.StatusFactory;
import org.epics.pvData.pv.StatusCreate;
import org.epics.pvData.pv.Status.StatusType;

/**
 * JUnit test for Status.
 * @author mse
 *
 */
public class StatusTest extends TestCase {
	
	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();

	public void testStatusOK()
	{
		assertSame(statusCreate.getStatusOK(), statusCreate.getStatusOK());
	}

	public void testStatusPrints()
	{
		System.out.println(statusCreate.getStatusOK());
		System.out.println(statusCreate.createStatus(StatusType.OK, null, null));
		System.out.println(statusCreate.createStatus(StatusType.WARNING, "warning", null));
		System.out.println(statusCreate.createStatus(StatusType.ERROR, "error", new RuntimeException("simple exception")));
		
		try {
			throw new RuntimeException("simulated cause");
		} catch (Throwable cause) {
			try {
				throw new RuntimeException("simulated exc", cause);
			} catch (Throwable th) {
				System.out.println(statusCreate.createStatus(StatusType.FATAL, "fatal", th));
			}
		}
	}
	
}
