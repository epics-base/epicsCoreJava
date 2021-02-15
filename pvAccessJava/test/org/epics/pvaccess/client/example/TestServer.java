package org.epics.pvaccess.client.example;

import org.epics.pvaccess.server.impl.remote.ServerContextImpl;
import org.epics.pvaccess.server.impl.remote.plugins.DefaultBeaconServerDataProvider;
import org.epics.pvaccess.server.test.TestChannelProviderImpl;

public class TestServer {

	public static void main(String[] args) {
		// Create a context with default configuration values.
		final ServerContextImpl context = new ServerContextImpl();
		context.setBeaconServerStatusProvider(new DefaultBeaconServerDataProvider(context));

		try {
			context.initialize(new TestChannelProviderImpl());
		} catch (Throwable th) {
			th.printStackTrace();
		}

		// Display basic information about the context.
        System.out.println(context.getVersion().getVersionString());
        context.printInfo(); System.out.println();

        new Thread(new Runnable() {

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
