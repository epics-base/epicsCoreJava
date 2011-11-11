package org.epics.ca.client.example;

import org.epics.ca.client.ChannelAccessFactory;
import org.epics.ca.client.ChannelProvider;
import org.epics.ca.server.impl.remote.ServerContextImpl;
import org.epics.ca.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.ca.server.test.TestChannelProviderImpl;

public class TestServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ChannelProvider channelProviderImpl = new TestChannelProviderImpl();
		ChannelAccessFactory.registerChannelProvider(channelProviderImpl);
		
		System.setProperty("EPICS4_CAS_PROVIDER_NAME", channelProviderImpl.getProviderName());
		
		// Create a context with default configuration values.
		final ServerContextImpl context = new ServerContextImpl();
		context.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(context));
		
		try {
			context.initialize(ChannelAccessFactory.getChannelAccess());
		} catch (Throwable th) {
			th.printStackTrace();
		}

		// Display basic information about the context.
        System.out.println(context.getVersion().getVersionString());
        context.printInfo(); System.out.println();

        new Thread(new Runnable() {
			
			@Override
			public void run() {
		        try {
	                System.out.println("Running server...");
					context.run(0);
	                System.out.println("Done.");
				} catch (Throwable th) {
	                System.out.println("Failure:");
					th.printStackTrace();
				}
			}
		}, "pvAccess server").start();
	}

}
