/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

/**
 * Instance allowing lock/unlock.
 * @author mse
 */
public interface Lockable {
	void lock();
	void unlock();
}
