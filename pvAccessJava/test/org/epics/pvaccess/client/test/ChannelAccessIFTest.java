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

package org.epics.pvaccess.client.test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.PVFactory;
import org.epics.pvaccess.client.AccessRights;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelArray;
import org.epics.pvaccess.client.ChannelArrayRequester;
import org.epics.pvaccess.client.ChannelFind;
import org.epics.pvaccess.client.ChannelFindRequester;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProcess;
import org.epics.pvaccess.client.ChannelProcessRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvaccess.client.ChannelPutGet;
import org.epics.pvaccess.client.ChannelPutGetRequester;
import org.epics.pvaccess.client.ChannelPutRequester;
import org.epics.pvaccess.client.ChannelRPC;
import org.epics.pvaccess.client.ChannelRPCRequester;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.client.GetFieldRequester;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.property.PVTimeStamp;
import org.epics.pvdata.property.PVTimeStampFactory;
import org.epics.pvdata.property.TimeStamp;
import org.epics.pvdata.property.TimeStampFactory;
import org.epics.pvdata.pv.Array;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.DoubleArrayData;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVArray;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Type;

import static org.junit.Assert.assertNotEquals;

/**
 * Channel Access IF test.
 * Requires "counter" (int, increases with 1Hz), "simpleCounter" (int), and "valueOnly" (double) standard channels.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public abstract class ChannelAccessIFTest extends TestCase {

	/**
	 * Get channel provider to be tested.
	 * @return the channel provider to be tested.
	 */
	public abstract ChannelProvider getChannelProvider();

	/**
	 * Get timeout value (in ms).
	 * @return the timeout value.
	 */
	public abstract long getTimeoutMs();

	/**
	 * Is local PVA implementation.
	 * @return local or not.
	 */
	public abstract boolean isLocal();

	protected final Set<Channel> channels = new HashSet<Channel>();

	/**
	 * If provider cannot be destroyed,
	 * channel must be registered so that is destroyed by tearDown().
	 * @param channel channel to destroy.
	 */
	public void registerChannelForDestruction(Channel channel)
	{
		if (channel == null)
			return;

		synchronized (channels) {
			channels.add(channel);
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		synchronized (channels) {
			for (Channel channel : channels) {
				try {
					if (channel.getConnectionState() != ConnectionState.DESTROYED)
						channel.destroy();
				} catch (Throwable th) { th.printStackTrace(); }
			}
			channels.clear();
		}
	}

	public void testTestImpl()
	{
		assertNotNull(getChannelProvider());
		assertSame("getChannelProvider() returns different instance for each call; fix the test",
				getChannelProvider(), getChannelProvider());
		assertTrue("getTimeoutMs() <= 0", getTimeoutMs() > 0);
	}

	public void testProviderName()
	{
		final ChannelProvider provider = getChannelProvider();

		assertNotNull(provider.getProviderName());
		assertSame("getProviderName() returns different String instance for each call", provider.getProviderName(), provider.getProviderName());
		assertTrue("empty getProviderName()", provider.getProviderName().length() > 0);
	}

	protected static class ChannelRequesterCreatedTestImpl implements ChannelRequester {

		public Channel channel;
		public Status status;
		public int createdCount = 0;
		public int stateChangeCount = 0;

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public synchronized void channelStateChange(Channel c, ConnectionState connectionState) {
			stateChangeCount++;
			this.notifyAll();
		}

		public synchronized void channelCreated(Status status, Channel channel) {
			createdCount++;
			this.status = status;
			this.channel = channel;
			this.notifyAll();
		}
	}

	protected static class ChannelFindRequesterTestImpl implements ChannelFindRequester {

		public Status status;
		public boolean wasFound;
		public int findCount = 0;

		public synchronized void channelFindResult(Status status, ChannelFind channelFind, boolean wasFound) {
			findCount++;
			this.status = status;
			this.wasFound = wasFound;
			this.notifyAll();
		}
	}

	public void testCreateChannel() throws Throwable
	{
		final ChannelProvider provider = getChannelProvider();

		//
		// null requester test, exception expected
		//
		try
		{
			provider.createChannel("someName", null, ChannelProvider.PRIORITY_DEFAULT);
			fail("null ChannelRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		Channel channel;

		//
		// null channel name
		//
		ChannelRequesterCreatedTestImpl crcti = new ChannelRequesterCreatedTestImpl();
		try
		{
			provider.createChannel(null, crcti, ChannelProvider.PRIORITY_DEFAULT);
			fail("null channel name accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
			synchronized (crcti) {
				assertEquals(0, crcti.createdCount);
			}
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		//
		// invalid priority value
		//
		crcti = new ChannelRequesterCreatedTestImpl();
		try
		{
			provider.createChannel("counter", crcti, (short)(ChannelProvider.PRIORITY_MIN - 1));
			fail("invalid priority accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
			synchronized (crcti) {
				assertEquals(0, crcti.createdCount);
			}
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		//
		// finally, create that channel
		//
		crcti = new ChannelRequesterCreatedTestImpl();
		synchronized (crcti) {
			channel = provider.createChannel("counter", crcti, (short)(ChannelProvider.PRIORITY_DEFAULT + 1));
			registerChannelForDestruction(channel);
			assertNotNull(channel);

			if (crcti.createdCount == 0)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			assertTrue(crcti.status.isSuccess());
			assertNotNull(crcti.channel);

			// let's be mean and wait for some time
			crcti.wait(1000);

			assertEquals(1, crcti.createdCount);
		}

		// local PVA does not recreate provider, so we don't want to destroy it
		if (isLocal())
			return;

		provider.destroy();

		//
		// request on destroyed provider
		//
		crcti = new ChannelRequesterCreatedTestImpl();
		synchronized (crcti) {
			channel = provider.createChannel("counter", crcti, ChannelProvider.PRIORITY_DEFAULT);
			registerChannelForDestruction(channel);
			assertNull(channel);

			if (crcti.createdCount == 0)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			assertFalse(crcti.status.isSuccess());
			assertNull(crcti.channel);
		}
	}


	public void testFindChannel() throws Throwable
	{
		final ChannelProvider provider = getChannelProvider();

		//
		// null requester test, exception expected
		//
		try
		{
			ChannelFind cf = provider.channelFind("someName", null);
			if (cf == null)
				return;	// not supported/implemented (e.g. client provider)
			fail("null ChannelFindRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		ChannelFind channelFind;

		//
		// null channel name
		//
		ChannelFindRequesterTestImpl cfrti = new ChannelFindRequesterTestImpl();
		try {
			provider.channelFind(null, cfrti);
			fail("null channel name accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
			synchronized (cfrti) {
				assertEquals(0, cfrti.findCount);
			}
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		//
		// finally, find that channel
		//
		cfrti = new ChannelFindRequesterTestImpl();
		synchronized (cfrti) {
			channelFind = provider.channelFind("counter", cfrti);
			if (channelFind == null)
				return;	// not supported/implemented (e.g. client provider)
			//assertNotNull(channelFind);

			if (cfrti.findCount == 0)
				cfrti.wait(getTimeoutMs());

			assertEquals(1, cfrti.findCount);
			assertTrue(cfrti.status.isSuccess());
			assertTrue(cfrti.wasFound);

			// let's be mean and wait for some time
			cfrti.wait(1000);

			assertEquals(1, cfrti.findCount);
		}


		//
		// finally, find non-existing channel
		//
		int count;
		cfrti = new ChannelFindRequesterTestImpl();
		synchronized (cfrti) {
			channelFind = provider.channelFind("nonExisting", cfrti);
			assertNotNull(channelFind);

			if (cfrti.findCount == 0)
				cfrti.wait(getTimeoutMs());

			// 2 possible scenarios

			// 1. immediate response (local PVA)
			count = cfrti.findCount;
			if (count == 1)
			{
				assertTrue(cfrti.status.isSuccess());
				assertFalse(cfrti.wasFound);
			}
			// 2. no response yet
			else if (count == 0)
			{
				// nothing to test
			}
			else
				fail("invalid count on channelFindResult() call");

			// let's be mean and wait for some time
			cfrti.wait(1000);

			assertEquals(count, cfrti.findCount);
		}

		// cancel...
		channelFind.cancel();

		// no more responses
		synchronized (cfrti) {
			// let's be mean and wait for some time
			cfrti.wait(1000);

			assertEquals(count, cfrti.findCount);
		}

		// local PVA does not recreate provider, so we don't want to destroy it
		if (isLocal())
			return;

		provider.destroy();

		//
		// request on destroyed provider
		//
		cfrti = new ChannelFindRequesterTestImpl();
		synchronized (cfrti) {
			channelFind = provider.channelFind("nonExisting", cfrti);
			assertNull(channelFind);

			if (cfrti.findCount == 0)
				cfrti.wait(getTimeoutMs());

			assertEquals(1, cfrti.findCount);
			assertFalse(cfrti.status.isSuccess());
			assertFalse(cfrti.wasFound);
		}
	}

	public void testChannel() throws Throwable
	{
		final ChannelProvider provider = getChannelProvider();

		Channel channel;
		ChannelRequesterCreatedTestImpl crcti = new ChannelRequesterCreatedTestImpl();
		synchronized (crcti) {
			channel = provider.createChannel("counter", crcti, (short)(ChannelProvider.PRIORITY_DEFAULT + 1));
			registerChannelForDestruction(channel);
			assertNotNull(channel);

			if (crcti.createdCount == 0)
				crcti.wait(getTimeoutMs());

			assertEquals("channelCreated not called", 1, crcti.createdCount);
			assertTrue(crcti.stateChangeCount <= 1);
			assertTrue(crcti.status.isSuccess());
			assertNotNull(crcti.channel);
		}

		// test accessors
		assertEquals("counter", channel.getChannelName());
		// TODO assertEquals((short)(ChannelProvider.PRIORITY_DEFAULT + 1), channel.getChannelPriority());
		assertSame(crcti, channel.getChannelRequester());
		assertEquals(crcti.getRequesterName(), channel.getRequesterName());
		assertSame(provider, channel.getProvider());

		/*
		// not yet connected
		assertNull(channel.getRemoteAddress());
		assertFalse(channel.isConnected());
		assertEquals(ConnectionState.NEVER_CONNECTED, channel.getConnectionState());

		// disconnect on never connected test
		channel.disconnect();
		// state should not change at all
		// not yet connected
		assertNull(channel.getRemoteAddress());
		assertFalse(channel.isConnected());
		assertEquals(ConnectionState.NEVER_CONNECTED, channel.getConnectionState());
		synchronized (crcti) {
			assertEquals(0, crcti.stateChangeCount);
		}
		*/

		// now we really connect
		synchronized (crcti) {
			//channel.connect();

			// wait if not connected
			if (crcti.stateChangeCount == 0)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			assertEquals("channel failed to connect (or no connect notification via channelStateChange)", 1, crcti.stateChangeCount);
			assertTrue(crcti.status.isSuccess());
		}
		assertNotNull(channel.getRemoteAddress());
		/*String channelRemoteAddress =*/ channel.getRemoteAddress();
		assertTrue(channel.isConnected());
		assertEquals(ConnectionState.CONNECTED, channel.getConnectionState());
		/*
		// duplicate connect, should be noop
		synchronized (crcti) {
			channel.connect();

			// wait for new event (failure)... or timeoutMs
			if (crcti.stateChangeCount == 1)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			assertEquals(1, crcti.stateChangeCount);
			assertTrue(crcti.status.isSuccess());
		}
		assertEquals(channelRemoteAddress, channel.getRemoteAddress());
		assertTrue(channel.isConnected());
		assertEquals(ConnectionState.CONNECTED, channel.getConnectionState());


		// now we disconnect
		synchronized (crcti) {
			channel.disconnect();

			// wait for new event (failure)... or timeoutMs
			if (crcti.stateChangeCount == 1)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			assertEquals("no disconnect notification via channelStateChange", 2, crcti.stateChangeCount);
			assertTrue(crcti.status.isSuccess());
		}
		assertNull(channel.getRemoteAddress());
		assertFalse(channel.isConnected());
		assertEquals(ConnectionState.DISCONNECTED, channel.getConnectionState());


		// double disconnect
		synchronized (crcti) {
			channel.disconnect();

			// wait for new event (failure)... or timeoutMs
			if (crcti.stateChangeCount == 2)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			assertEquals(2, crcti.stateChangeCount);
			assertTrue(crcti.status.isSuccess());
		}
		assertNull(channel.getRemoteAddress());
		assertFalse(channel.isConnected());
		assertEquals(ConnectionState.DISCONNECTED, channel.getConnectionState());
		*/



		// ... and finally we destroy
		synchronized (crcti) {
			channel.destroy();

			// wait for new event (failure)... or timeoutMs
			if (crcti.stateChangeCount == 1)
			//if (crcti.stateChangeCount == 2)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			// disconnected might be called first
			assertTrue("no destroy notification via channelStateChange", 2 <= crcti.stateChangeCount);
			//assertTrue("no destroy notification via channelStateChange", 3 <= crcti.stateChangeCount);
			assertTrue(crcti.status.isSuccess());
		}
//		assertNull(channel.getRemoteAddress());
		assertFalse(channel.isConnected());
		assertEquals(ConnectionState.DESTROYED, channel.getConnectionState());

		final int count = crcti.stateChangeCount;

		// of course, we do destroy-again test
		synchronized (crcti) {
			// noop expected
			channel.destroy();

			// wait for new event (failure)... or timeoutMs
			if (crcti.stateChangeCount == count)
				crcti.wait(getTimeoutMs());

			assertEquals(1, crcti.createdCount);
			// we allow warning message
			if (crcti.status.getType() != StatusType.WARNING)
				assertEquals(count, crcti.stateChangeCount);
		}
//		assertNull(channel.getRemoteAddress());
		assertFalse(channel.isConnected());
		assertEquals(ConnectionState.DESTROYED, channel.getConnectionState());

		// just send a message...
		channel.message("testing 1, 2, 3...", MessageType.info);
	}

	public void testChannelAccessRights() throws Throwable {
		if (isLocal())
		{
		    Channel ch = syncCreateChannel("valueOnly");

		    // TODO for now just call with null (meaning all?)
		    assertEquals(AccessRights.readWrite, ch.getAccessRights(null));

		    ch.destroy();
		}
	}


	/** ----------------------- REQUEST TESTS  -----------------------**/

    private class ConnectionListener implements ChannelRequester
    {
    	private Boolean notification = null;

 		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelRequester#channelCreated(org.epics.pvdata.pv.Status, org.epics.pvaccess.client.Channel)
		 */
		public void channelCreated(Status status,
				org.epics.pvaccess.client.Channel channel) {
			/*
			if (status.isSuccess())
				channel.connect();
			else {
			*/
			if (!status.isSuccess()) {
				synchronized (this) {
					notification = Boolean.FALSE;
					this.notify();
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelRequester#channelStateChange(org.epics.pvaccess.client.Channel, org.epics.pvaccess.client.Channel.ConnectionState)
		 */
		public void channelStateChange(
				org.epics.pvaccess.client.Channel c,
				ConnectionState connectionStatus) {
 			synchronized (this) {
				notification = connectionStatus == ConnectionState.CONNECTED;
				this.notify();
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

		public void waitAndCheck() {
			synchronized (this) {
				final long timeOutMs = getTimeoutMs()*10;
				final long t1 = System.currentTimeMillis();
				final long timeout = t1+timeOutMs;

				// Wait for the status to change or time out
				while (notification == null && System.currentTimeMillis() < timeout)
				{
					try {
						this.wait(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				assertNotNull("channel connect timeout", notification);
				assertTrue("channel not connected", notification);
			}
		}
    }

	protected Channel syncCreateChannel(String name, boolean waitForConnectEvent) throws Throwable
	{
		ConnectionListener cl = new ConnectionListener();
	    Channel ch = getChannelProvider().createChannel(name, cl, PVAConstants.PVA_DEFAULT_PRIORITY);
	    registerChannelForDestruction(ch);
	    if (waitForConnectEvent) cl.waitAndCheck();
		return ch;
	}

	protected Channel syncCreateChannel(String name) throws Throwable
	{
		return syncCreateChannel(name, true);
	}

    private static PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();

    private class ChannelGetRequesterImpl implements ChannelGetRequester
	{
		ChannelGet channelGet;
		BitSet bitSet;
		PVStructure pvStructure;
		Structure structure;

		private Boolean connected = null;
		private Boolean success = null;

		public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
			synchronized (this) {
				this.channelGet = channelGet;
				this.structure = structure;

				connected = status.isOK();
				this.notify();
			}
		}

		public void waitAndCheckConnect()
		{
			waitAndCheckConnect(true);
		}

		public void waitAndCheckConnect(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel get connect timeout", connected);
				if (expectedSuccess) {
					assertTrue("channel get failed to connect", connected.booleanValue());
					assertNotNull(channelGet);
					assertNotNull(structure);
				}
				else {
					assertFalse("channel get has not failed to connect", connected.booleanValue());
				}
			}
		}

		public void syncGet(boolean lastRequest)
		{
			syncGet(lastRequest, true);
		}

		public void syncGet(boolean lastRequest, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel get not connected", connected);

				success = null;
				if (lastRequest)
					channelGet.lastRequest();
				channelGet.get();

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel get timeout", success);
				if (expectedSuccess)
					assertTrue("channel get failed", success.booleanValue());
				else
					assertFalse("channel get has not failed", success.booleanValue());
			}
		}

		public void getDone(Status success, ChannelGet channelGet, PVStructure pvStructure, BitSet bitSet) {
			synchronized (this) {
				this.success = success.isOK();
				this.bitSet = bitSet;
				this.pvStructure = pvStructure;
				this.notify();
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

	}

	private class ChannelPutRequesterImpl implements ChannelPutRequester
	{
		ChannelPut channelPut;
		PVStructure pvStructure;
		Structure structure;
		BitSet bitSet;

		private Boolean connected = null;
		private Boolean success = null;

		public void channelPutConnect(Status status, ChannelPut channelPut, Structure structure) {
			synchronized (this) {
				this.channelPut = channelPut;
				this.structure = structure;

				connected = status.isOK();
				this.notify();
			}
		}

		public void waitAndCheckConnect()
		{
			waitAndCheckConnect(true);
		}

		public void waitAndCheckConnect(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel put connect timeout", connected);
				if (expectedSuccess) {
					assertTrue("channel put failed to connect", connected.booleanValue());
					assertNotNull(channelPut);
					assertNotNull(structure);
				}
				else {
					assertFalse("channel put has not failed to connect", connected.booleanValue());
				}
			}
		}

		public void syncGet()
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel put not connected", connected);

				success = null;
				channelPut.get();

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel get timeout", success);
				assertTrue("channel get failed", success.booleanValue());
			}
		}

		public void getDone(Status success, ChannelPut channelPut, PVStructure pvStructure, BitSet bitSet) {
			synchronized (this) {
				this.success = success.isOK();
				this.pvStructure = pvStructure;
				this.bitSet = bitSet;
				this.notify();
			}
		}

		public void syncPut(boolean lastRequest, PVStructure pvPutStructure, BitSet pvPutBitSet)
		{
			syncPut(lastRequest, pvPutStructure, pvPutBitSet, true);
		}

		public void syncPut(boolean lastRequest, PVStructure pvPutStructure, BitSet pvPutBitSet, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel put not connected", connected);

				success = null;
				if (lastRequest)
					channelPut.lastRequest();
				channelPut.put(pvPutStructure, pvPutBitSet);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel put timeout", success);
				if (expectedSuccess)
					assertTrue("channel put failed", success.booleanValue());
				else
					assertFalse("channel put has not failed", success.booleanValue());
			}
		}

		public void putDone(Status success, ChannelPut channelPut) {
			synchronized (this) {
				this.success = success.isOK();
				this.notify();
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}
	}


	private class ChannelPutGetRequesterImpl implements ChannelPutGetRequester
	{
		ChannelPutGet channelPutGet;
		PVStructure pvPutStructure;
		PVStructure pvGetStructure;
		// TODO
		//BitSet pvPutBitSet;
		//BitSet pvGetBitSet;

		Structure putStructure;
		Structure getStructure;

		private Boolean connected = null;
		private Boolean success = null;

		/* (non-Javadoc)
		 * @see org.epics.pvaccess.client.ChannelPutGetRequester#channelPutGetConnect(Status,org.epics.pvaccess.client.ChannelPutGet, org.epics.pvdata.pv.PVStructure, org.epics.pvdata.pv.PVStructure)
		 */
		public void channelPutGetConnect(
				Status status,
				ChannelPutGet channelPutGet,
				Structure putStructure, Structure getStructure) {
			synchronized (this)
			{
				this.channelPutGet = channelPutGet;
				this.putStructure = putStructure;
				this.getStructure = getStructure;

				connected = status.isOK();
				this.notify();
			}
		}


		public void waitAndCheckConnect()
		{
			waitAndCheckConnect(true);
		}

		public void waitAndCheckConnect(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel put-get connect timeout", connected);
				if (expectedSuccess) {
					assertTrue("channel put-get failed to connect", connected);
					assertNotNull(channelPutGet);
					assertNotNull(putStructure);
					assertNotNull(getStructure);
				} else {
					assertFalse("channel put-get has not failed to connect", connected);
					assertNull(pvPutStructure);
					assertNull(pvGetStructure);
				}
			}
		}

		public void syncPutGet(boolean lastRequest, PVStructure pvPutGetStructure, BitSet pvPutGetBitSet)
		{
			syncPutGet(lastRequest, pvPutGetStructure, pvPutGetBitSet, true);
		}

		public void syncPutGet(boolean lastRequest,
				PVStructure pvPutGetStructure, BitSet pvPutGetBitSet, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null) {
					assertNotNull("channel put-get not connected", connected);
				}

				success = null;
				if (lastRequest)
					channelPutGet.lastRequest();
				channelPutGet.putGet(pvPutGetStructure, pvPutGetBitSet);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel put-get timeout", success);
				if (expectedSuccess)
					assertTrue("channel put-get failed", success);
				else
					assertFalse("channel put-get has not failed", success);
			}
		}

		public void putGetDone(Status success, ChannelPutGet channelPutGet, PVStructure pvGetStructure, BitSet pvGetBitSet) {
			synchronized (this) {
				this.success = success.isOK();
				this.pvGetStructure = pvGetStructure;
				this.notify();
			}
		}

		public void syncGetGet()
		{
			syncGetGet(true);
		}

		public void syncGetGet(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel put-get not connected", connected);

				success = null;
				channelPutGet.getGet();

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel get-get timeout", success);
				if (expectedSuccess)
					assertTrue("channel get-get failed", success);
				else
					assertFalse("channel get-get has not failed", success);
			}
		}

		public void getGetDone(Status success, ChannelPutGet channelPutGet, PVStructure pvGetStructure, BitSet pvGetBitSet) {
			synchronized (this) {
				this.success = success.isOK();
				this.pvGetStructure = pvGetStructure;
				this.notify();
			}
		}

		public void syncGetPut()
		{
			syncGetPut(true);
		}
		public void syncGetPut(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel put-get not connected", connected);

				success = null;
				channelPutGet.getPut();

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel get-put timeout", success);
				if (expectedSuccess)
					assertTrue("channel get-put failed", success);
				else
					assertFalse("channel get-put has not failed", success);
			}
		}

		public void getPutDone(Status success, ChannelPutGet channelPutGet, PVStructure pvPutStructure, BitSet pvPutBitSet) {
			synchronized (this) {
				this.success = success.isOK();
				this.pvPutStructure = pvPutStructure;
				this.notify();
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

	}

	private class ChannelRPCRequesterImpl implements ChannelRPCRequester
	{
		ChannelRPC channelRPC;
		PVStructure result;

		private Boolean connected = null;
		private Boolean success = null;

		public void channelRPCConnect(Status status, ChannelRPC channelRPC) {
			synchronized (this)
			{
				this.channelRPC = channelRPC;

				connected = status.isOK();
				this.notify();
			}
		}


		public void requestDone(Status status, ChannelRPC channelRPC, PVStructure pvResponse) {
			synchronized (this) {
				this.success = status.isOK();
				this.result = pvResponse;
				this.notify();
			}
		}

		public void waitAndCheckConnect()
		{
			waitAndCheckConnect(true);
		}

		public void waitAndCheckConnect(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel rpc connect timeout", connected);
				if (expectedSuccess) {
					assertTrue("channel rpc failed to connect", connected);
				} else {
					assertFalse("channel rpc has not failed to connect", connected);
				}
			}
		}

		public PVStructure syncRPC(PVStructure arguments, boolean lastRequest)
		{
			return syncRPC(arguments, lastRequest, true);
		}

		public PVStructure syncRPC(PVStructure arguments, boolean lastRequest, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel rpc not connected", connected);

				success = null;
				result = null;
				if (lastRequest)
					channelRPC.lastRequest();
				channelRPC.request(arguments);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel rpc timeout", success);
				if (expectedSuccess)
					assertTrue("channel rpc failed", success);
				else
					assertFalse("channel rpc has not failed", success);

				return result;
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}
	}

	ChannelProcessRequester channelProcessRequester = new ChannelProcessRequester() {

		public void processDone(Status success, ChannelProcess channelProcess) {
			channelProcess.lastRequest();
			channelProcess.process();
		}

		public void channelProcessConnect(Status status, ChannelProcess channelProcess) {
			channelProcess.process();
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

	};

	private class GetFieldRequesterImpl implements GetFieldRequester {

		Field field;

		private Boolean success;

		public void getDone(Status status, Field field) {
			synchronized (this) {
				this.field = field;
				this.success = status.isOK();
				this.notify();
			}
		}

		public void syncGetField(Channel ch, String subField)
		{
			syncGetField(ch, subField, true);
		}

		public void syncGetField(Channel ch, String subField, boolean expectedSuccess)
		{
			synchronized (this) {

				success = null;
				ch.getField(this, subField);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel getField timeout", success);
				if (expectedSuccess)
					assertTrue("channel getField failed", success);
				else
					assertFalse("channel getField has not failed", success);
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}
	}

	private class ChannelProcessRequesterImpl implements ChannelProcessRequester {

		ChannelProcess channelProcess;

		private Boolean success;
		private Boolean connected;

		public void processDone(Status success, ChannelProcess channelProcess) {
			synchronized (this) {
				this.success = success.isOK();
				this.notify();
			}
		}

		public void channelProcessConnect(Status status, ChannelProcess channelProcess) {
			synchronized (this) {
				this.channelProcess = channelProcess;

				connected = status.isOK();
				this.notify();
			}
		}

		public void waitAndCheckConnect()
		{
			waitAndCheckConnect(true);
		}

		public void waitAndCheckConnect(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel process connect timeout", connected);
				if (expectedSuccess)
					assertTrue("channel process failed to connect", connected);
				else
					assertFalse("channel process has not failed to connect", connected);
			}
		}

		public void syncProcess(boolean lastRequest)
		{
			syncProcess(lastRequest, true);
		}

		public void syncProcess(boolean lastRequest, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel process not connected", connected);

				success = null;
				if (lastRequest)
					channelProcess.lastRequest();
				channelProcess.process();

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel process timeout", success);
				if (expectedSuccess)
					assertTrue("channel process failed", success);
				else
					assertFalse("channel process has not failed", success);
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

	}


	private class ChannelArrayRequesterImpl implements ChannelArrayRequester {

		ChannelArray channelArray;
		PVArray pvArray;
		Array array;

		int length = -1;

		private Boolean connected = null;
		private Boolean success = null;

		public void channelArrayConnect(Status status, ChannelArray channelArray, Array array) {
			synchronized (this)
			{
				this.channelArray = channelArray;
				this.array = array;
				connected = status.isOK();
				this.notify();
			}
		}

		public void waitAndCheckConnect()
		{
			waitAndCheckConnect(true);
		}

		public void waitAndCheckConnect(boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel array connect timeout", connected);
				if (expectedSuccess)
				{
					assertTrue("channel array failed to connect", connected);
					assertNotNull(channelArray);
					assertNotNull(array);
				}
				else
				{
					assertFalse("channel array has not failed to connect", connected);
					assertNull(pvArray);
				}
			}
		}

		public void getArrayDone(Status success, ChannelArray channelArray, PVArray pvArray) {
			synchronized (this) {
				this.success = success.isOK();
				this.pvArray = pvArray;
				this.notify();
			}
		}

		public void syncGet(boolean lastRequest, int offset, int count, int stride)
		{
			syncGet(lastRequest, offset, count, stride, true);
		}

		public void syncGet(boolean lastRequest, int offset, int count, int stride, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel array not connected", connected);

				success = null;
				if (lastRequest)
					channelArray.lastRequest();
				// TODO stride
				channelArray.getArray(offset, count, stride);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel array get timeout", success);
				if (expectedSuccess)
					assertTrue("channel array get failed", success);
				else
					assertFalse("channel array get has not failed", success);
			}
		}

		public void putArrayDone(Status success, ChannelArray channelArray) {
			synchronized (this) {
				this.success = success.isOK();
				this.notify();
			}
		}

		public void syncPut(boolean lastRequest, PVArray pvArray, int offset, int count, int stride)
		{
			syncPut(lastRequest, pvArray, offset, count, stride, true);
		}

		public void syncPut(boolean lastRequest, PVArray pvArray, int offset, int count, int stride, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel array not connected", connected);

				success = null;
				if (lastRequest)
					channelArray.lastRequest();
				channelArray.putArray(pvArray, offset, count, stride);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel array put timeout", success);
				if (expectedSuccess)
					assertTrue("channel array put failed", success);
				else
					assertFalse("channel array put has not failed", success);
			}
		}

		public void syncSetLength(boolean lastRequest, int length)
		{
			syncSetLength(lastRequest, length, true);
		}

		public void syncSetLength(boolean lastRequest, int length, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel array not connected", connected);

				success = null;
				if (lastRequest)
					channelArray.lastRequest();
				channelArray.setLength(length);

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel array setLength timeout", success);
				if (expectedSuccess)
					assertTrue("channel array setLength failed", success);
				else
					assertFalse("channel array setLength has not failed", success);
			}
		}

		public void setLengthDone(Status status, ChannelArray channelArray) {
			synchronized (this) {
				this.success = status.isOK();
				this.notify();
			}
		}

		public void syncGetLength(boolean lastRequest)
		{
			syncGetLength(lastRequest, true);
		}

		public void syncGetLength(boolean lastRequest, boolean expectedSuccess)
		{
			synchronized (this) {
				if (connected == null)
					assertNotNull("channel array not connected", connected);

				success = null;
				if (lastRequest)
					channelArray.lastRequest();
				channelArray.getLength();

				try {
					if (success == null)
						this.wait(getTimeoutMs());
				} catch (InterruptedException e) {
					// noop
				}

				assertNotNull("channel array getLength timeout", success);
				if (expectedSuccess)
					assertTrue("channel array getLength failed", success);
				else
					assertFalse("channel array getLength has not failed", success);
			}
		}

		public void getLengthDone(Status status, ChannelArray channelArray,
				int length) {
			synchronized (this) {
				this.success = status.isOK();
				this.length = length;
				this.notify();
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}

	}

	private class ChannelMonitorRequesterImpl implements MonitorRequester {

		PVStructure pvStructure;
		BitSet changeBitSet;
		BitSet overrunBitSet;

		AtomicInteger monitorCounter = new AtomicInteger();

		Monitor channelMonitor;

		private Boolean connected = null;

		public void monitorConnect(Status status, Monitor channelMonitor, Structure structure) {
			synchronized (this)
			{
				this.channelMonitor = channelMonitor;

				connected = status.isOK();
				this.notify();
			}
		}

		public void waitAndCheckConnect()
		{
			synchronized (this) {
				if (connected == null)
				{
					try {
						this.wait(getTimeoutMs());
					} catch (InterruptedException e) {
						// noop
					}
				}

				assertNotNull("channel monitor connect timeout", connected);
				assertTrue("channel monitor failed to connect", connected);
			}
		}

		public void unlisten(Monitor monitor) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.monitor.MonitorRequester#monitorEvent(org.epics.pvdata.monitor.Monitor)
		 */
		public void monitorEvent(Monitor monitor) {
			synchronized (this) {
				MonitorElement monitorElement = monitor.poll();

				this.pvStructure = monitorElement.getPVStructure();
				this.changeBitSet = monitorElement.getChangedBitSet();
				this.overrunBitSet = monitorElement.getOverrunBitSet();

				assertNotNull(this.pvStructure);
				assertNotNull(this.changeBitSet);
				assertNotNull(this.overrunBitSet);

				monitorCounter.incrementAndGet();
				this.notify();

				monitor.release(monitorElement);
			}
		}

		public String getRequesterName() {
			return this.getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			System.err.println("[" + messageType + "] " + message);
		}
	}

	public void testChannelGet() throws Throwable
	{
	    Channel ch = syncCreateChannel("valueOnly");

	    channelGetTestParameters(ch);

		channelGetTestNoProcess(ch, false);
		//channelGetTestNoProcess(ch, true);

		ch.destroy();

	    ch = syncCreateChannel("simpleCounter");

		channelGetTestIntProcess(ch, false);
		//channelGetTestIntProcess(ch, true);

		channelGetTestNoConnection(ch, true);
		channelGetTestNoConnection(ch, false);
		/*
		ch.destroy();
		channelGetTestNoConnection(ch, false);
		*/

		ch = syncCreateChannel("simpleCounter", false);
		// channel not yet connected
		channelGetTestIntProcess(ch, false);
	}

	private void channelGetTestParameters(Channel ch) throws Throwable
	{
		CreateRequest createRequest = CreateRequest.create();
		String request = "field(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
    	try
        {
        	ch.createChannelGet(null, pvRequest);
			fail("null ChannelGetRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl();
		try
        {
        	ch.createChannelGet(channelGetRequester, null);
			fail("null pvRequest accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

	}

	private void channelGetTestNoConnection(Channel ch, boolean disconnect) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "field(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
		ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl();
		ch.createChannelGet(channelGetRequester, pvRequest);
		channelGetRequester.waitAndCheckConnect(disconnect);
		if (disconnect)
		{
			//ch.disconnect();
			ch.destroy();
			channelGetRequester.syncGet(false, false);
		}
	}

	private void channelGetTestNoProcess(Channel ch, boolean share) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "field(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}

		ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl();
		ch.createChannelGet(channelGetRequester, pvRequest);
		channelGetRequester.waitAndCheckConnect();

		//assertEquals("get-test", channelGetRequester.pvStructure.getFullName());

		channelGetRequester.syncGet(false);
		// only first bit must be set
		assertEquals(1, channelGetRequester.bitSet.cardinality());
		assertTrue(channelGetRequester.bitSet.get(0));

		channelGetRequester.syncGet(false);
		// no changes
		assertEquals(0, channelGetRequester.bitSet.cardinality());

		channelGetRequester.syncGet(true);
		// no changes, again
		assertEquals(0, channelGetRequester.bitSet.cardinality());

		channelGetRequester.channelGet.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelGetRequester.syncGet(true, false);
	}

	private void channelGetTestIntProcess(Channel ch, boolean share) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "record[process=true]field(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}

		ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl();
		ch.createChannelGet(channelGetRequester, pvRequest);
		channelGetRequester.waitAndCheckConnect();

		//assertEquals("get-test", channelGetRequester.pvStructure.getFullName());
		channelGetRequester.syncGet(false);
		PVInt value = channelGetRequester.pvStructure.getIntField("value");
		PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
		assertTrue(pvTimeStamp.attach(channelGetRequester.pvStructure.getStructureField("timeStamp")));
		TimeStamp timestamp = TimeStampFactory.create();
		pvTimeStamp.get(timestamp);
		// only first bit must be set
		assertEquals(1, channelGetRequester.bitSet.cardinality());
		assertTrue(channelGetRequester.bitSet.get(0));

		// multiple tests
		final int TIMES = 3;
		for (int i = 1; i <= TIMES; i++)
		{
			int previousValue = value.get();
			long previousTimestampSec = timestamp.getSecondsPastEpoch();

			// 2 seconds to have different timestamps
			Thread.sleep(1000);

			channelGetRequester.syncGet(i == TIMES);
			pvTimeStamp.get(timestamp);
			// changes of value and timeStamp
			assertEquals((previousValue + 1)%11, value.get());
			assertTrue(timestamp.getSecondsPastEpoch() > previousTimestampSec);
		}

		channelGetRequester.channelGet.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelGetRequester.syncGet(true, false);
	}

	public void testChannelPut() throws Throwable
	{
	    Channel ch = syncCreateChannel("valueOnly");

	    channelPutTestParameters(ch);

		channelPutTestNoProcess(ch, false);
		//channelPutTestNoProcess(ch, true);

		ch.destroy();

	    ch = syncCreateChannel("simpleCounter");

		channelPutTestIntProcess(ch, false);
		//channelPutTestIntProcess(ch, true);

		channelPutTestNoConnection(ch, true);
		channelPutTestNoConnection(ch, false);
		/*
		ch.destroy();
		channelPutTestNoConnection(ch, false);
		*/
	    ch = syncCreateChannel("simpleCounter", false);
	    // channel not yet connected
		channelPutTestIntProcess(ch, false);
	}

	private void channelPutTestParameters(Channel ch) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "field(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        try
        {
        	ch.createChannelPut(null, pvRequest);
			fail("null ChannelPutRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		ChannelPutRequesterImpl channelPutRequester = new ChannelPutRequesterImpl();
		try
        {
        	ch.createChannelPut(channelPutRequester, null);
			fail("null pvRequest accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

	}

	private void channelPutTestNoConnection(Channel ch, boolean disconnect) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "field(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
		ChannelPutRequesterImpl channelPutRequester = new ChannelPutRequesterImpl();
		ch.createChannelPut(channelPutRequester, pvRequest);
		channelPutRequester.waitAndCheckConnect(disconnect);
		if (disconnect)
		{
			//ch.disconnect();
			ch.destroy();
			channelPutRequester.syncPut(false, null, null, false);
		}
	}

	private void channelPutTestNoProcess(Channel ch, boolean share) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "field(value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
		ChannelPutRequesterImpl channelPutRequester = new ChannelPutRequesterImpl();
		ch.createChannelPut(channelPutRequester, pvRequest);
		channelPutRequester.waitAndCheckConnect();

		//assertEquals("put-test", channelPutRequester.pvStructure.getFullName());

		channelPutRequester.syncGet();

		// set and get test
		PVDouble value = channelPutRequester.pvStructure.getDoubleField("value");
		assertNotNull(value);
		final double INIT_VAL = 123.0;
		value.put(INIT_VAL);
		channelPutRequester.bitSet.set(value.getFieldOffset());

		channelPutRequester.syncPut(false, channelPutRequester.pvStructure, channelPutRequester.bitSet);
		// TODO should put bitSet be reset here
		//assertEquals(0, channelPutRequester.bitSet.cardinality());
		channelPutRequester.syncGet();
		assertEquals(INIT_VAL, value.get());


		// value should not change since bitSet is not set
		// unless it is shared and local
		value.put(INIT_VAL+2);
		channelPutRequester.bitSet.clear();
		channelPutRequester.syncPut(false, channelPutRequester.pvStructure, channelPutRequester.bitSet);
		channelPutRequester.syncGet();
		if (share && isLocal())
			assertEquals(INIT_VAL+2, value.get());
		else
			assertEquals(INIT_VAL, value.get());

		// now should change
		value.put(INIT_VAL+1);
		channelPutRequester.bitSet.set(value.getFieldOffset());
		channelPutRequester.syncPut(false, channelPutRequester.pvStructure, channelPutRequester.bitSet);
		channelPutRequester.syncGet();
		assertEquals(INIT_VAL+1, value.get());

		// destroy
		channelPutRequester.syncPut(true, channelPutRequester.pvStructure, channelPutRequester.bitSet);

		channelPutRequester.channelPut.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelPutRequester.syncPut(true, channelPutRequester.pvStructure, channelPutRequester.bitSet, false);
	}

	private void channelPutTestIntProcess(Channel ch, boolean share) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "record[process=true]field(value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
		ChannelPutRequesterImpl channelPutRequester = new ChannelPutRequesterImpl();
		ch.createChannelPut(channelPutRequester, pvRequest);
		channelPutRequester.waitAndCheckConnect();

		//assertEquals("put-test", channelPutRequester.pvStructure.getFullName());

		channelPutRequester.syncGet();

		// set and get test
		PVInt value = channelPutRequester.pvStructure.getIntField("value");
		assertNotNull(value);
		final int INIT_VAL = 3;
		value.put(INIT_VAL);
		channelPutRequester.bitSet.set(value.getFieldOffset());

		channelPutRequester.syncPut(false, channelPutRequester.pvStructure, channelPutRequester.bitSet);
		// TODO should put bitSet be reset here
		//assertEquals(0, channelPutRequester.bitSet.cardinality());
		channelPutRequester.syncGet();
		assertEquals(INIT_VAL+1, value.get());	// +1 due to process


		// value should change only due to process
		value.put(INIT_VAL+3);
		channelPutRequester.bitSet.clear();
		channelPutRequester.syncPut(false, channelPutRequester.pvStructure, channelPutRequester.bitSet);
		channelPutRequester.syncGet();
		if (share && isLocal())
			assertEquals(INIT_VAL+4, value.get());
		else
			assertEquals(INIT_VAL+2, value.get());

		// destroy
		channelPutRequester.syncPut(true, channelPutRequester.pvStructure, channelPutRequester.bitSet);

		channelPutRequester.channelPut.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelPutRequester.syncPut(true, channelPutRequester.pvStructure, channelPutRequester.bitSet, false);
	}

	public void testChannelGetField() throws Throwable
	{
	    Channel ch = syncCreateChannel("simpleCounter");

		GetFieldRequesterImpl channelGetField = new GetFieldRequesterImpl();

		// get all
		channelGetField.syncGetField(ch, null);
		assertNotNull(channelGetField.field);
		assertEquals(Type.structure, channelGetField.field.getType());
		// TODO there is no name
		// assertEquals(ch.getChannelName(), channelGetField.field.getFieldName());

		// value only
		channelGetField.syncGetField(ch, "value");
		assertNotNull(channelGetField.field);
		assertEquals(Type.scalar, channelGetField.field.getType());

		// non-existent
		channelGetField.syncGetField(ch, "invalid", false);
		assertNull(channelGetField.field);

		/*
		ch.disconnect();

		channelGetField.syncGetField(ch, "value", false);
		*/

		ch.destroy();

		channelGetField.syncGetField(ch, "value", false);
	}

	public void testChannelProcess() throws Throwable
	{
	    Channel ch = syncCreateChannel("simpleCounter");

		// create get to check processing
    	CreateRequest createRequest = CreateRequest.create();
		String request = "field(value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
		ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl();
		ch.createChannelGet(channelGetRequester, pvRequest);
		channelGetRequester.waitAndCheckConnect();

		// get initial state
		channelGetRequester.syncGet(false);

		// null requester test
        try
        {
        	ch.createChannelProcess(null, null);
			fail("null ChannelProcessRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		// create process
		ChannelProcessRequesterImpl channelProcessRequester = new ChannelProcessRequesterImpl();
		ch.createChannelProcess(channelProcessRequester, null);
		channelProcessRequester.waitAndCheckConnect();

		// there should be no changes
		channelGetRequester.syncGet(false);
		assertEquals(0, channelGetRequester.bitSet.cardinality());

		channelProcessRequester.syncProcess(false);

		// there should be a change
		channelGetRequester.syncGet(false);
		assertEquals(1, channelGetRequester.bitSet.cardinality());
		/*
		// now let's try to create another processor :)
		ChannelProcessRequesterImpl channelProcessRequester2 = new ChannelProcessRequesterImpl();
		ch.createChannelProcess(channelProcessRequester2, null);
		channelProcessRequester2.waitAndCheckConnect();

		// and process
		channelProcessRequester2.syncProcess(false);

		// there should be a change
		channelGetRequester.syncGet(false);
		assertEquals(1, channelGetRequester.bitSet.cardinality());

		// TODO since there is no good error handling I do not know that creating of second process failed !!!
		// however it shouldn't, right!!!

		// check if process works with destroy option
		channelProcessRequester.syncProcess(true);

		// there should be a change
		channelGetRequester.syncGet(false);
		assertEquals(1, channelGetRequester.bitSet.cardinality());
		 */
		channelProcessRequester.channelProcess.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelProcessRequester.syncProcess(true, false);

		channelProcessTestNoConnection(ch, true);
		channelProcessTestNoConnection(ch, false);
		/*
		ch.destroy();
		channelProcessTestNoConnection(ch, false);
		*/

	    ch = syncCreateChannel("simpleCounter", false);
		// channel not yet connected
		channelProcessRequester = new ChannelProcessRequesterImpl();
		ch.createChannelProcess(channelProcessRequester, null);
		channelProcessRequester.waitAndCheckConnect();

	}

	private void channelProcessTestNoConnection(Channel ch, boolean disconnect) throws Throwable
	{
		ChannelProcessRequesterImpl channelProcessRequester = new ChannelProcessRequesterImpl();
		ch.createChannelProcess(channelProcessRequester, null);
		channelProcessRequester.waitAndCheckConnect(disconnect);
		if (disconnect)
		{
			//ch.disconnect();
			ch.destroy();
			channelProcessRequester.syncProcess(false, false);
		}
	}
	/*
	public void testChannelTwoGetProcess() throws Throwable
	{
		Channel ch = syncCreateChannel("simpleCounter");

		// create gets to check processing
    	PVStructure pvRequest = CreateRequestFactory.createRequest("record[process=true]field(value)",ch);

		ChannelGetRequesterImpl channelGetRequester = new ChannelGetRequesterImpl();
		ch.createChannelGet(channelGetRequester, pvRequest, "get-process-test", true, true, null);
		channelGetRequester.waitAndCheckConnect();

		// get initial state
		channelGetRequester.syncGet(false);

		// there should be a change
		channelGetRequester.syncGet(false);
		assertEquals(1, channelGetRequester.bitSet.cardinality());

		// another get
		ChannelGetRequesterImpl channelGetRequester2 = new ChannelGetRequesterImpl();
		ch.createChannelGet(channelGetRequester2, pvRequest, "get-process-test-2", true, true, null);
		channelGetRequester2.waitAndCheckConnect();

		// get initial state
		channelGetRequester2.syncGet(false);

		// there should be a change too
		channelGetRequester2.syncGet(false);
		assertEquals(1, channelGetRequester2.bitSet.cardinality());

		// TODO since there is no good error handling I do not know that creating of second process failed !!!
		// however it shouldn't, right!!!
	}
	*/

	public void testChannelPutGet() throws Throwable
	{
	    Channel ch = syncCreateChannel("valueOnly");

	    channelPutGetTestParameters(ch);

		channelPutGetTestNoProcess(ch, false);
		//channelPutGetTestNoProcess(ch, true);

		ch.destroy();

	    ch = syncCreateChannel("simpleCounter");

		channelPutGetTestIntProcess(ch, false);
		//channelPutGetTestIntProcess(ch, true);

		channelPutGetTestNoConnection(ch, true);
		channelPutGetTestNoConnection(ch, false);

		/*
		ch.destroy();
		channelPutGetTestNoConnection(ch, false);
		*/

	    ch = syncCreateChannel("simpleCounter", false);
		// channel not yet connected
		channelPutGetTestIntProcess(ch, false);
	}

	private void channelPutGetTestParameters(Channel ch) throws Throwable
	{
       	CreateRequest createRequest = CreateRequest.create();
		String request = "putField(value)getField(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        try
        {
        	ch.createChannelPutGet(null, pvRequest);
			fail("null ChannelProcessRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		ChannelPutGetRequesterImpl channelPutGetRequester = new ChannelPutGetRequesterImpl();
		try
        {
        	ch.createChannelPutGet(channelPutGetRequester, null);
			fail("null pvRequest accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}
	}

	private void channelPutGetTestNoConnection(Channel ch, boolean disconnect) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "putField(value)getField(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
		ChannelPutGetRequesterImpl channelPutGetRequester = new ChannelPutGetRequesterImpl();
		ch.createChannelPutGet(channelPutGetRequester, pvRequest);
		channelPutGetRequester.waitAndCheckConnect(disconnect);
		if (disconnect)
		{
			//ch.disconnect();
			ch.destroy();
			channelPutGetRequester.syncPutGet(false, null, null, false);
			channelPutGetRequester.syncGetGet(false);
			channelPutGetRequester.syncGetPut(false);
		}
	}

	private void channelPutGetTestNoProcess(Channel ch, boolean share) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "putField(value)getField(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        ChannelPutGetRequesterImpl channelPutGetRequester = new ChannelPutGetRequesterImpl();
		ch.createChannelPutGet(channelPutGetRequester, pvRequest);
		channelPutGetRequester.waitAndCheckConnect();

		//assertEquals("put-test", channelPutGetRequester.pvPutStructure.getFullName());
		//assertEquals("get-test", channelPutGetRequester.pvGetStructure.getFullName());

		channelPutGetRequester.syncGetPut();
		channelPutGetRequester.syncGetGet();

		// set and get test
		PVDouble putValue = channelPutGetRequester.pvPutStructure.getDoubleField("value");
		assertNotNull(putValue);
		final double INIT_VAL = 321.0;
		putValue.put(INIT_VAL);

		PVDouble getValue = channelPutGetRequester.pvGetStructure.getDoubleField("value");
		assertNotNull(getValue);

		BitSet entireStructure = new BitSet(channelPutGetRequester.pvPutStructure.getNumberFields());
		entireStructure.set(0);

		channelPutGetRequester.syncPutGet(false, channelPutGetRequester.pvPutStructure, entireStructure);
		assertEquals(INIT_VAL, getValue.get());

		// again
		putValue.put(INIT_VAL+1);
		channelPutGetRequester.syncPutGet(false, channelPutGetRequester.pvPutStructure, entireStructure);
		assertEquals(INIT_VAL+1, getValue.get());

		// test get-put
		channelPutGetRequester.syncGetPut();
		// TODO

		// test get-get
		channelPutGetRequester.syncGetGet();
		// TODO

		// destroy
		channelPutGetRequester.syncPutGet(true, channelPutGetRequester.pvPutStructure, entireStructure);

		channelPutGetRequester.channelPutGet.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelPutGetRequester.syncPutGet(true, channelPutGetRequester.pvPutStructure, entireStructure, false);
	}

	private void channelPutGetTestIntProcess(Channel ch, boolean share) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "record[process=true]putField(value)getField(timeStamp,value)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        ChannelPutGetRequesterImpl channelPutGetRequester = new ChannelPutGetRequesterImpl();
		ch.createChannelPutGet(channelPutGetRequester, pvRequest);
		channelPutGetRequester.waitAndCheckConnect();

		//assertEquals("put-test", channelPutGetRequester.pvPutStructure.getFullName());
		//assertEquals("get-test", channelPutGetRequester.pvGetStructure.getFullName());

		channelPutGetRequester.syncGetPut();
		channelPutGetRequester.syncGetGet();

		BitSet entireStructure = new BitSet(channelPutGetRequester.pvPutStructure.getNumberFields());
		entireStructure.set(0);


		// set and get test
		PVInt putValue = channelPutGetRequester.pvPutStructure.getIntField("value");
		assertNotNull(putValue);
		final int INIT_VAL = 3;
		putValue.put(INIT_VAL);

		PVInt getValue = channelPutGetRequester.pvGetStructure.getIntField("value");
		assertNotNull(getValue);
		PVTimeStamp pvTimeStamp = PVTimeStampFactory.create();
        assertTrue(pvTimeStamp.attach(channelPutGetRequester.pvGetStructure.getStructureField("timeStamp")));
        TimeStamp timestamp = TimeStampFactory.create();
		// get all
		channelPutGetRequester.syncGetGet();
        pvTimeStamp.get(timestamp);

		// multiple tests
		final int TIMES = 3;
		for (int i = 1; i <= TIMES; i++)
		{
			int previousValue = getValue.get();
			long previousTimestampSec = timestamp.getSecondsPastEpoch();

			putValue.put((previousValue + 1)%11);

			// 2 seconds to have different timestamps
			Thread.sleep(1500);

			channelPutGetRequester.syncPutGet(i == TIMES, channelPutGetRequester.pvPutStructure, entireStructure);
	        pvTimeStamp.get(timestamp);

	        // changes of value and timeStamp; something is not right here...
			assertEquals((previousValue + 1 + 1)%11, getValue.get());	// +1 (new value) +1 (process)
			assertTrue(timestamp.getSecondsPastEpoch() > previousTimestampSec);
		}

		channelPutGetRequester.channelPutGet.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelPutGetRequester.syncPutGet(true, channelPutGetRequester.pvPutStructure, entireStructure, false);
	}

	public void testChannelRPC() throws Throwable
	{
	    Channel ch = syncCreateChannel("sum");

	    channelRPCTestParameters(ch);

		channelRPCTest(ch);

		channelRPCTestNoConnection(ch, true);
		channelRPCTestNoConnection(ch, false);


		ch = syncCreateChannel("sum", false);
		// channel not yet connected
		channelRPCTest(ch);
	}

	private void channelRPCTestParameters(Channel ch) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        try
        {
        	ch.createChannelRPC(null, pvRequest);
			fail("null ChannelRPCRequesterImpl accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		/*
		ChannelRPCRequesterImpl channelRPCRequester = new ChannelRPCRequesterImpl();
		try
        {
        	ch.createRPC(channelRPCRequester, null);
			fail("null pvRequest accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}
		*/
	}

    private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();

    private static PVStructure createRPCArguments()
	{
		PVStructure args;
		{
	        Field[] fields = new Field[2];
	        fields[0] = fieldCreate.createScalar(ScalarType.pvDouble);
	        fields[1] = fieldCreate.createScalar(ScalarType.pvDouble);
	        args = pvDataCreate.createPVStructure(fieldCreate.createStructure(new String[] { "a", "b" }, fields));
		}

		args.getDoubleField("a").put(12.3);
		args.getDoubleField("b").put(45.6);

		return args;
	}

    private void channelRPCTest(Channel ch)
    {
    	PVStructure pvRequest = null; //CreateRequestFactory.createRequest("",ch);
    	PVStructure arguments = createRPCArguments();

		ChannelRPCRequesterImpl channelRPCRequester = new ChannelRPCRequesterImpl();
		ch.createChannelRPC(channelRPCRequester, pvRequest);
		channelRPCRequester.waitAndCheckConnect();

		PVStructure result = channelRPCRequester.syncRPC(arguments, false);
		assertNotNull(result);
		PVDouble c = result.getDoubleField("c");
		assertNotNull(c);
		assertEquals(12.3+45.6, c.get());

		channelRPCRequester.channelRPC.destroy();

		channelRPCRequester.syncRPC(arguments, false, false);
    }

    private void channelRPCTestNoConnection(Channel ch, boolean disconnect) throws Throwable
	{
    	PVStructure pvRequest = null; //CreateRequestFactory.createRequest("",ch);
    	PVStructure arguments = createRPCArguments();

		ChannelRPCRequesterImpl channelRPCRequester = new ChannelRPCRequesterImpl();
		ch.createChannelRPC(channelRPCRequester, pvRequest);
		channelRPCRequester.waitAndCheckConnect(disconnect);
		if (disconnect)
		{
			//ch.disconnect();
			ch.destroy();
			channelRPCRequester.syncRPC(arguments, false, false);
		}
	}

	private void channelArrayTestParameters(Channel ch) throws Throwable
	{
    	CreateRequest createRequest = CreateRequest.create();
		String request = "value";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        try
        {
        	ch.createChannelArray(null, pvRequest);
			fail("null ChannelArrayRequesterImpl accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}


		ChannelArrayRequesterImpl channelArrayRequester = new ChannelArrayRequesterImpl();
		try
        {
        	ch.createChannelArray(channelArrayRequester, null);
			fail("null pvRequest accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

	}

	public void testChannelArray() throws Throwable
	{
	    Channel ch = syncCreateChannel("arrayDouble");

	    channelArrayTestParameters(ch);

	    // TODO !!
//    	PVStructure pvRequest = CreateRequestFactory.createRequest("field(value)",ch);
	    CreateRequest createRequest = CreateRequest.create();
		String request = "value";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}

    	ChannelArrayRequesterImpl channelArrayRequester = new ChannelArrayRequesterImpl();
	    ch.createChannelArray(channelArrayRequester, pvRequest);
	    channelArrayRequester.waitAndCheckConnect();

	    // test get
	    channelArrayRequester.syncGet(true, 1, 2, 1);
	    PVDoubleArray array = (PVDoubleArray)channelArrayRequester.pvArray;
	    DoubleArrayData data = new DoubleArrayData();
	    int count = array.get(0, 100, data);
	    assertEquals(2, count);
	    assertEquals(2.2, data.data[0]);
	    assertEquals(3.3, data.data[1]);

	    ch.destroy();

		channelArrayRequester.channelArray.destroy();
		// this must fail (callback with unsuccessful completion status)
		channelArrayRequester.syncGet(true, 1, 2, 1, false);

    	//pvFieldName.put("value");
	    ch = syncCreateChannel("arrayDouble");
	    channelArrayRequester = new ChannelArrayRequesterImpl();
	    ch.createChannelArray(channelArrayRequester, pvRequest);
	    channelArrayRequester.waitAndCheckConnect();

	    // test put
	    final double[] ARRAY_VALUE = new double[] { 1.1, 2.2, 3.3, 4.4, 5.5 };
	    channelArrayRequester.syncGet(false, 0, ARRAY_VALUE.length /*0*/, 1); // this allows multiple runs on the same JavaIOC
	    PVDoubleArray doubleArray = (PVDoubleArray)channelArrayRequester.pvArray;
	    doubleArray.put(0, ARRAY_VALUE.length, ARRAY_VALUE, 0);
	    channelArrayRequester.syncPut(false, channelArrayRequester.pvArray, 0, 0, 1);
	    channelArrayRequester.syncGet(false, 0, ARRAY_VALUE.length /*0*/, 1); // this allows multiple runs on the same JavaIOC
	    DoubleArrayData doubleData = new DoubleArrayData();
	    count = doubleArray.get(0, 100, doubleData);
	    assertEquals(ARRAY_VALUE.length, count);
	    for (int i = 0; i < count; i++)
	    	assertEquals(ARRAY_VALUE[i], doubleData.data[i]);

	    channelArrayRequester.syncPut(false, channelArrayRequester.pvArray, 4, 0, 1);	// result: 1.1, 2.2, 3.3, 4.4, 1.1, 2.2, 3.3, 4.4, 5.5
	    channelArrayRequester.syncGet(false, 3, 3, 1);
	    count = doubleArray.get(0, 3, doubleData);
	    assertEquals(3, count);
	    final double[] EXPECTED_VAL = new double[] { 4.4, 1.1, 2.2 };
	    for (int i = 0; i < count; i++)
	    	assertEquals(EXPECTED_VAL[i], doubleData.data[i]);

	    channelArrayRequester.syncSetLength(false, 3);  // result: 1.1, 2.2, 3.3
	    channelArrayRequester.syncGet(false, 0, 0, 1);
	    count = doubleArray.get(0, 1000, doubleData);
	    assertEquals(3, count);
	    for (int i = 0; i < count; i++)
	    	assertEquals(ARRAY_VALUE[i], doubleData.data[i]);

	    // big array test
	    final int BIG_CAPACITY = 10000;
	    channelArrayRequester.syncSetLength(false, BIG_CAPACITY);
	    channelArrayRequester.syncGet(false, 0, 0, 1);
	    count = doubleArray.get(0, 10000, doubleData);
	    assertEquals(10000, count);
	    for (int i = 0; i < 3; i++)
	    	assertEquals(ARRAY_VALUE[i], doubleData.data[i]);
	    for (int i = 3; i < count; i++)
	    	assertEquals(0.0, doubleData.data[i]);

	    // test setLength and  getLength
	    final int NEW_LEN = BIG_CAPACITY/2;
	    channelArrayRequester.syncSetLength(false, NEW_LEN);
	    channelArrayRequester.syncGetLength(false);
	    assertEquals(NEW_LEN, channelArrayRequester.length);

		channelArrayTestNoConnection(ch, true);
		channelArrayTestNoConnection(ch, false);
		/*
		ch.destroy();
		channelArrayTestNoConnection(ch, false);
		*/

	    ch = syncCreateChannel("arrayDouble", false);
	    // channel not yet connected
	    channelArrayRequester = new ChannelArrayRequesterImpl();
	    ch.createChannelArray(channelArrayRequester, pvRequest);
	    channelArrayRequester.waitAndCheckConnect();
	}

	private void channelArrayTestNoConnection(Channel ch, boolean disconnect) throws Throwable
	{
		ChannelArrayRequesterImpl channelArrayRequester = new ChannelArrayRequesterImpl();

		//    	PVStructure pvRequest = CreateRequestFactory.createRequest("field(value)", channelArrayRequester);
		CreateRequest createRequest = CreateRequest.create();
		String request = "value";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}

    	ch.createChannelArray(channelArrayRequester, pvRequest);
		channelArrayRequester.waitAndCheckConnect(disconnect);
		if (disconnect)
		{
			//ch.disconnect();
			ch.destroy();
			channelArrayRequester.syncGet(false, 1, 2, 1, false);
			channelArrayRequester.syncPut(false, channelArrayRequester.pvArray, 1, 2, 1, false);
			channelArrayRequester.syncSetLength(false, 1, false);
		}
	}

	private static final Convert convert = ConvertFactory.getConvert();

	public void testChannelMonitors() throws Throwable
	{
        Channel ch = syncCreateChannel("counter");
    	CreateRequest createRequest = CreateRequest.create();
		String request = "record[queueSize=3]field(timeStamp)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
        // null requester test
        try
        {
        	ch.createMonitor(null, pvRequest);
			fail("null MonitorRequester accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

        // null pvRequest test
	    ChannelMonitorRequesterImpl channelMonitorRequester = new ChannelMonitorRequesterImpl();
        try
        {
        	ch.createMonitor(channelMonitorRequester, null);
			fail("null pvRequest accepted");
		} catch (AssertionFailedError afe) {
			throw afe;
		} catch (IllegalArgumentException th) {
			// OK
		} catch (Throwable th) {
			fail("other than IllegalArgumentException exception was thrown");
		}

		ch.destroy();

		// TODO uncomment
		//channelMonitorTest(10);
		//channelMonitorTest(2);
		channelMonitorTest(1);
//		channelMonitorTest(0);


        ch = syncCreateChannel("counter", false);
        // channel not yet connected
        channelMonitorRequester = new ChannelMonitorRequesterImpl();
	    ch.createMonitor(channelMonitorRequester, pvRequest);
	    channelMonitorRequester.waitAndCheckConnect();

	}

	public void channelMonitorTest(int queueSize) throws Throwable
	{
        Channel ch = syncCreateChannel("counter");
    	String request = "record[queueSize=" + queueSize + "]field(timeStamp,value)";
    	// TODO algorithm
    	CreateRequest createRequest = CreateRequest.create();
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
    	ChannelMonitorRequesterImpl channelMonitorRequester = new ChannelMonitorRequesterImpl();
	    ch.createMonitor(channelMonitorRequester, pvRequest);
	    channelMonitorRequester.waitAndCheckConnect();

	    // not start, no monitors
	    assertEquals(0, channelMonitorRequester.monitorCounter.get());

	    synchronized (channelMonitorRequester) {
		    channelMonitorRequester.channelMonitor.start();

		    if (channelMonitorRequester.monitorCounter.get() == 0)
		    	channelMonitorRequester.wait(getTimeoutMs());
		    //assertEquals("monitor-test", channelMonitorRequester.pvStructure.getFullName());
		    assertEquals(1, channelMonitorRequester.monitorCounter.get());
		    assertEquals(1, channelMonitorRequester.changeBitSet.cardinality());
		    assertTrue(channelMonitorRequester.changeBitSet.get(0));

		    PVField valueField = channelMonitorRequester.pvStructure.getSubField("value");
		    PVField previousValue = pvDataCreate.createPVField(valueField.getField());
		    convert.copy(valueField, previousValue);
			assertEquals(valueField, previousValue);

		    // all subsequent only timestamp and value
		    for (int i = 2; i < 5; i++) {
			    channelMonitorRequester.wait(getTimeoutMs());
			    //assertEquals("monitor-test", channelMonitorRequester.pvStructure.getFullName());
			    assertEquals(i, channelMonitorRequester.monitorCounter.get());
			    if (queueSize == 1)
			    {
				    assertEquals(1, channelMonitorRequester.changeBitSet.cardinality());
				    assertTrue(channelMonitorRequester.changeBitSet.get(0));
			    }
			    else
			    {
				    assertEquals(2, channelMonitorRequester.changeBitSet.cardinality());
				    assertTrue(channelMonitorRequester.changeBitSet.get(1));
				    assertTrue(channelMonitorRequester.changeBitSet.get(4));
			    }

			    valueField = channelMonitorRequester.pvStructure.getSubField("value");
				assertNotEquals(valueField, previousValue);
			    convert.copy(valueField, previousValue);
		    }

		    channelMonitorRequester.channelMonitor.stop();
		    channelMonitorRequester.wait(500);
		    int mc = channelMonitorRequester.monitorCounter.get();
		    Thread.sleep(2000);
		    // no more monitors
		    assertEquals(mc, channelMonitorRequester.monitorCounter.get());
	    }
	    ch.destroy();
	}

	// ----------------- stress tests --------------

    public void testStressConnectDisconnect() throws Throwable
    {
    	final int COUNT = 300;
    	for (int i = 1; i <= COUNT; i++)
    	{
    		Channel channel = syncCreateChannel("valueOnly");
    		channel.destroy();
    	}
    }

    public void testStressConnectGetDisconnect() throws Throwable
    {
    	final int COUNT = 300;
    	for (int i = 1; i <= COUNT; i++)
    	{
    		ChannelGetRequesterImpl channelGetRequesterImpl = new ChannelGetRequesterImpl();
    		CreateRequest createRequest = CreateRequest.create();
    		String request = "field(value)";
        	PVStructure pvRequest = createRequest.createRequest(request);
        	if(pvRequest==null) {
        		String message ="createRequest failed " + createRequest.getMessage();
        		throw new IllegalArgumentException(message);
        	}
    		Channel channel = syncCreateChannel("valueOnly", false);
    		channel.createChannelGet(channelGetRequesterImpl, pvRequest);
    		channelGetRequesterImpl.waitAndCheckConnect();
    		channelGetRequesterImpl.syncGet(true);

    		channel.destroy();
    	}
    }

    public void testStressMonitorAndProcess() throws Throwable
    {
        Channel ch = syncCreateChannel("simpleCounter");

    	CreateRequest createRequest = CreateRequest.create();
		String request = "record[queueSize=3]field(timeStamp,value,alarm.severity.choices)";
    	PVStructure pvRequest = createRequest.createRequest(request);
    	if(pvRequest==null) {
    		String message ="createRequest failed " + createRequest.getMessage();
    		throw new IllegalArgumentException(message);
    	}
    	ChannelMonitorRequesterImpl channelMonitorRequester = new ChannelMonitorRequesterImpl();
	    ch.createMonitor(channelMonitorRequester, pvRequest);
	    channelMonitorRequester.waitAndCheckConnect();

	    // not start, no monitors
	    assertEquals(0, channelMonitorRequester.monitorCounter.get());

	    synchronized (channelMonitorRequester) {
		    channelMonitorRequester.channelMonitor.start();

		    if (channelMonitorRequester.monitorCounter.get() == 0)
		    	channelMonitorRequester.wait(getTimeoutMs());
		    assertEquals(1, channelMonitorRequester.monitorCounter.get());
	    }

		// create process
		ChannelProcessRequesterImpl channelProcessRequester = new ChannelProcessRequesterImpl();
		ch.createChannelProcess(channelProcessRequester, null);
		channelProcessRequester.waitAndCheckConnect();

		final int COUNT = 50000;
		for (int i = 2; i < COUNT; i++)
		{
			channelProcessRequester.syncProcess(false);
			synchronized (channelMonitorRequester) {
			    if (channelMonitorRequester.monitorCounter.get() < i)
			    	channelMonitorRequester.wait(getTimeoutMs());
				assertEquals(i, channelMonitorRequester.monitorCounter.get());
			}
		}

    }

	// ----------------- ... and at last destroy() --------------

	public void testDestroy() throws Throwable
	{
		final ChannelProvider provider = getChannelProvider();

		provider.destroy();

		//
		// multiple destroy test
		//
		// noop expected
		provider.destroy();
	}

	// last here, always performed last
	public void testFinalize() throws Throwable
	{
		internalFinalize();
	}

	protected void internalFinalize() throws Throwable
	{
		// noop
	}
}
