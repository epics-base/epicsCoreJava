/*
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.event.ContextExceptionEvent;
import gov.aps.jca.event.ContextExceptionListener;
import gov.aps.jca.event.ContextMessageEvent;
import gov.aps.jca.event.ContextMessageListener;
import gov.aps.jca.event.ContextVirtualCircuitExceptionEvent;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelListRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderFactory;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.ThreadReady;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;

/**
 * Factory and implementation of Channel Access V3 client.
 * By default CAJ is used, but context can be changed by setting
 * system property named by <code>JCA_CONTEXT_CLASS_PROPERTY_NAME</code>
 * (e.g. <code>org.epics.ca.ClientFactory.jcaContextClass</code>).
 * @author mrk
 *
 */
public class ClientFactory  {
    private static ChannelProviderImpl channelProvider = null;
    private static final ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
	private static ChannelProviderFactoryImpl factory = null;

    public static final String PROVIDER_NAME = "ca";

    public static final String JCA_CONTEXT_CLASS_PROPERTY_NAME = ClientFactory.class.getName() + ".jcaContextClass";

    private static class ChannelProviderFactoryImpl implements ChannelProviderFactory
    {

		public String getFactoryName() {
			return PROVIDER_NAME;
		}

		public synchronized ChannelProvider sharedInstance() {
	        try
	        {
	        	if (channelProvider == null)
	        		channelProvider = new ChannelProviderImpl();

				return channelProvider;
	        } catch (Throwable e) {
	            throw new RuntimeException("Failed to initialize shared CA client instance.", e);
	        }
		}

		public ChannelProvider newInstance() {
	        try
	        {
				return new ChannelProviderImpl();
	        } catch (Throwable e) {
	            throw new RuntimeException("Failed to initialize new CA client instance.", e);
	        }
		}

		public synchronized boolean destroySharedInstance() {
			boolean destroyed = true;
			if (channelProvider != null)
			{
    				try{
    				    	channelProvider.destroy();
    				    	channelProvider = null;
    				} catch (Exception ex) {
    				    	destroyed = false;
    				}
			}
			return destroyed;
		}
    }

    /**
     * Registers CA client channel provider factory.
     */
    public static synchronized void start() {
        if (factory != null) return;
        factory = new ChannelProviderFactoryImpl();
        ChannelProviderRegistryFactory.registerChannelProviderFactory(factory);
    }

    /**
     * Unregisters CA client channel provider factory and destroys shared channel provider instance (if necessary).
     */
    public static synchronized void stop() {
    	if (factory != null)
    	{
    		ChannelProviderRegistryFactory.unregisterChannelProviderFactory(factory);
    		if(factory.destroySharedInstance())
    		{
    			factory=null;
    		}
    	}
    }

    private static class ChannelProviderImpl
    implements ChannelProvider,ContextExceptionListener, ContextMessageListener
    {
        private final Context context;
        private final CAThread caThread;

        ChannelProviderImpl() {
        	Context c = null;
            try {
            	String contextClass = System.getProperty(JCA_CONTEXT_CLASS_PROPERTY_NAME, JCALibrary.CHANNEL_ACCESS_JAVA);
            	c = JCALibrary.getInstance().createContext(contextClass);
            } catch (Throwable e) {
                e.printStackTrace();
                context = null;
                caThread = null;
                return;
            }
            context = c;

            CAThread t;
            try {
                context.addContextExceptionListener(this);
                context.addContextMessageListener(this);
                t = new CAThread("ca",ThreadPriority.getJavaPriority(ThreadPriority.low), context);
            } catch (Throwable e) {
                e.printStackTrace();
                caThread = null;
                return;
            }
            caThread = t;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.ChannelProvider#destroy()
         */
        public void destroy() {
            caThread.stop();
            try {
                context.destroy();
            } catch (CAException e) {
                e.printStackTrace();
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#channelFind(java.lang.String, org.epics.pvaccess.client.ChannelFindRequester)
         */
        public ChannelFind channelFind(String channelName,ChannelFindRequester channelFindRequester) {
            LocateFind locateFind = new LocateFind(this,channelName,context);
            locateFind.find(channelFindRequester);
            return locateFind;
        }

        private static final Status listNotSupported =
        	StatusFactory.getStatusCreate()
        		.createStatus(StatusType.ERROR, "channelList not supported", null);

    	private ChannelFind channelFind =
    		new ChannelFind() {

    			public ChannelProvider getChannelProvider() {
    				return ChannelProviderImpl.this;
    			}

    			public void cancel() {
    				// noop
    			}
    		};

		public ChannelFind channelList(ChannelListRequester channelListRequester) {
        	channelListRequester.channelListResult(listNotSupported, channelFind, null, false);
			return channelFind;
		}

		/* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short)
         */
        public Channel createChannel(String channelName,
                ChannelRequester channelRequester, short priority)
        {
            LocateFind locateFind = new LocateFind(this,channelName,context);
            return locateFind.create(channelRequester);
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short, java.lang.String)
         */
		public Channel createChannel(String channelName,
				ChannelRequester channelRequester, short priority,
				String address) {
        	if (address != null)
        		throw new IllegalArgumentException("address not allowed for CA implementation");
			return createChannel(channelName, channelRequester, priority);
		}
		/* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.ChannelProvider#getProviderName()
         */
        public String getProviderName() {
            return PROVIDER_NAME;
        }
        /* (non-Javadoc)
         * @see gov.aps.jca.event.ContextExceptionListener#contextException(gov.aps.jca.event.ContextExceptionEvent)
         */
        public void contextException(ContextExceptionEvent arg0) {
            String message = arg0.getMessage();
            System.err.println(message);
            System.err.flush();
        }
        /* (non-Javadoc)
         * @see gov.aps.jca.event.ContextExceptionListener#contextVirtualCircuitException(gov.aps.jca.event.ContextVirtualCircuitExceptionEvent)
         */
        public void contextVirtualCircuitException(ContextVirtualCircuitExceptionEvent arg0) {
            String message = "status " + arg0.getStatus().toString();
            System.err.println(message);
            System.err.flush();
        }
        /* (non-Javadoc)
         * @see gov.aps.jca.event.ContextMessageListener#contextMessage(gov.aps.jca.event.ContextMessageEvent)
         */
        public void contextMessage(ContextMessageEvent arg0) {
            String message = arg0.getMessage();
            System.out.println(message);
            System.out.flush();
        }
    }

    private static class LocateFind implements ChannelFind,ChannelFindRequester{

        private final ChannelProvider channelProvider;
        private volatile ChannelFindRequester channelFindRequester = null;
        private volatile BaseV3Channel v3Channel = null;
        private final String channelName;
        private final Context context;


        LocateFind(ChannelProvider channelProvider, String channelName, Context context) {
        	this.channelProvider = channelProvider;
            this.channelName = channelName;
            this.context = context;
        }

        void find(ChannelFindRequester channelFindRequester) {
            this.channelFindRequester = channelFindRequester;
            v3Channel = new BaseV3Channel(channelProvider,
                    this,null,context,channelName);
            v3Channel.connectCaV3();
        }

        Channel create(ChannelRequester channelRequester) {
            v3Channel = new BaseV3Channel(channelProvider,
                    null,channelRequester,context,channelName);
            v3Channel.connectCaV3();
            return v3Channel;
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelFind#cancel()
         */
        public void cancel() {
            v3Channel.destroy();
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelFind#getChannelProvider()
         */
        public ChannelProvider getChannelProvider() {
            return channelProvider;
        }

        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelFindRequester#channelFindResult(Stayus,org.epics.pvaccess.client.ChannelFind, boolean)
         */
        public void channelFindResult(Status status, ChannelFind channelFind, boolean wasFound) {
            channelFindRequester.channelFindResult(status, channelFind, wasFound);
            v3Channel.destroy();
        }
    }

    private static class CAThread implements RunnableReady {
        private final Thread thread;
        private final Context context;
        private CAThread(String threadName,int threadPriority, Context context)
        {
            this.context = context;
            this.thread = threadCreate.create(threadName, threadPriority, this);
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.RunnableReady#run(org.epics.ioc.util.ThreadReady)
         */
        public void run(ThreadReady threadReady) {
            threadReady.ready();
            try {
                while(true) {
                    try {
                        context.poll();
                    } catch (CAException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                    Thread.sleep(5);
                }
            } catch(InterruptedException e) {

            }
        }

        private void stop() {
            thread.interrupt();
        }
    }
}
