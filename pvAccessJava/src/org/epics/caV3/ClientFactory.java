/**
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.caV3;

import gov.aps.jca.CAException;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.event.ContextExceptionEvent;
import gov.aps.jca.event.ContextExceptionListener;
import gov.aps.jca.event.ContextMessageEvent;
import gov.aps.jca.event.ContextMessageListener;
import gov.aps.jca.event.ContextVirtualCircuitExceptionEvent;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelAccessFactory;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvdata.misc.RunnableReady;
import org.epics.pvdata.misc.ThreadCreate;
import org.epics.pvdata.misc.ThreadCreateFactory;
import org.epics.pvdata.misc.ThreadPriority;
import org.epics.pvdata.misc.ThreadReady;
import org.epics.pvdata.pv.Status;




/**
 * Factory and implementation of Channel Access V3 client.
 * @author mrk
 *
 */
public class ClientFactory  {
    static final ChannelProviderImpl channelProvider = new ChannelProviderImpl();
    private static Context context = null;
    private static final ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();

    public static final String PROVIDER_NAME = "caV3";

    /**
     * This registers the V3 ChannelProvider.
     */
    public static void start() {
        channelProvider.register();
    }
    
    private static class ChannelProviderImpl
    implements ChannelProvider,ContextExceptionListener, ContextMessageListener
    {
        private volatile boolean isRegistered = false; 
        private volatile CAThread caThread = null;
        
        synchronized private void register() {
            if(isRegistered) return;
            isRegistered = true;
            try {
                context = JCALibrary.getInstance().createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
                context.addContextExceptionListener(this);
                context.addContextMessageListener(this);
                caThread = new CAThread("cav3",ThreadPriority.getJavaPriority(ThreadPriority.low));
            } catch (CAException e) {
                System.err.println(e.getMessage());
                return;
            }     
            ChannelAccessFactory.registerChannelProvider(this);
        }       
        /* (non-Javadoc)
         * @see org.epics.ioc.channelAccess.ChannelProvider#destroy()
         */
        @Override
        public void destroy() {
            caThread.stop();
            try {
                context.destroy();
            } catch (CAException e) {
                System.err.println(e.getMessage());
            }
            ChannelAccessFactory.unregisterChannelProvider(this);
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#channelFind(java.lang.String, org.epics.pvaccess.client.ChannelFindRequester)
         */
        @Override
        public ChannelFind channelFind(String channelName,ChannelFindRequester channelFindRequester) {
            LocateFind locateFind = new LocateFind(this,channelName);
            locateFind.find(channelFindRequester);
            return locateFind;
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short)
         */
        @Override
        public Channel createChannel(String channelName,
                ChannelRequester channelRequester, short priority)
        {
            LocateFind locateFind = new LocateFind(this,channelName);
            return locateFind.create(channelRequester);
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelProvider#createChannel(java.lang.String, org.epics.pvaccess.client.ChannelRequester, short, java.lang.String)
         */
        @Override
		public Channel createChannel(String channelName,
				ChannelRequester channelRequester, short priority,
				String address) {
        	if (address != null)
        		throw new IllegalArgumentException("address not allowed for caV3 implementation");
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
        
        
        LocateFind(ChannelProvider channelProvider, String channelName) {
        	this.channelProvider = channelProvider;
            this.channelName = channelName;
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
         * @see org.epics.pvaccess.client.ChannelFind#cancelChannelFind()
         */
        @Override
        public void cancelChannelFind() {
            v3Channel.destroy();
        }
        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelFind#getChannelProvider()
         */
        @Override
        public ChannelProvider getChannelProvider() {
            return channelProvider;
        }

        /* (non-Javadoc)
         * @see org.epics.pvaccess.client.ChannelFindRequester#channelFindResult(Stayus,org.epics.pvaccess.client.ChannelFind, boolean)
         */
        @Override
        public void channelFindResult(Status status, ChannelFind channelFind, boolean wasFound) {
            channelFindRequester.channelFindResult(status, channelFind, wasFound);
            v3Channel.destroy();
        }
    }
    
    private static class CAThread implements RunnableReady {
        private final Thread thread;
        private CAThread(String threadName,int threadPriority)
        {
            thread = threadCreate.create(threadName, threadPriority, this);
        }         
        /* (non-Javadoc)
         * @see org.epics.ioc.util.RunnableReady#run(org.epics.ioc.util.ThreadReady)
         */
        public void run(ThreadReady threadReady) {        
System.out.println("CaV3Client");
context.printInfo();
System.out.println();
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
