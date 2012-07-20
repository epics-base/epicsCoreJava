/*
 * Copyright (c) 2009 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.pvaccess;

import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.impl.remote.ClientContextImpl;

public class ClientFactory {
	
	/**
	 * Name if the provider this factory registers.
	 */
	public static final String PROVIDER_NAME = ClientContextImpl.PROVIDER_NAME;

	static private boolean isRegistered = false; 
    static private ClientContextImpl context;
    
    /**
     * This initializes the Channel Access client.
     */
    public static synchronized void start() {
        if(isRegistered) return;
        
        try {
        	context = new ClientContextImpl();
			context.initialize();
            ChannelAccessFactory.registerChannelProvider(context.getProvider());
            isRegistered = true;
        } catch (Throwable e) {
        	stop();
            throw new RuntimeException("Failed to initializa client channel access.", e);
        }
    }
    
    /**
     * Stop the Channel Access client.
     */
    public static synchronized void stop() {
    	if (context != null)
    	{
    		context.dispose();
    		ChannelAccessFactory.unregisterChannelProvider(context.getProvider());
        	// allows GC to cleanup
        	context = null;
    	}
    	isRegistered = false;
    }
}