/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;


/**
 * <code>ChanneProvider</code> factory interface.
 * @author mse
 */
public interface ChannelProviderFactory {
	/**
	 * Get factory name (i.e. name of the provider).
	 * @return the factory name.
	 */
	String getFactoryName();

	/**
	 * Get a shared instance.
	 * @return a shared instance.
	 */
	ChannelProvider sharedInstance();

	/**
	 * Create a new instance.
	 * @return a new instance.
	 */
	ChannelProvider newInstance();
}
