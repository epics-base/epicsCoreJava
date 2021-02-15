/*
 *
 */
package org.epics.pvaccess.impl.remote.io.impl;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.epics.pvaccess.impl.remote.io.PollEvents;
import org.epics.pvaccess.impl.remote.io.Poller;
import java.nio.channels.SelectableChannel;

/**
 * @author msekoranja
 *
 */
public class PollerImpl implements Poller, Runnable {

	final Selector selector;

	// wake-up time kills low-latency, this mechanism loops selectNow for some time
	private int trottle = 0;

	public PollerImpl() throws IOException {
		selector = Selector.open();
	}

	public void start() {
		Thread t = new Thread(this,"PollerImpl-");
		//t.setPriority(Thread.NORM_PRIORITY);
		t.start();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.Poller#add(java.nio.channels.SelectableChannel, com.cosylab.jam.io.PollEvents, int)
	 */
	public void add(SelectableChannel channel, PollEvents handler, int ops)
	{
		registrations.add(new RegistrationRequest(channel, handler, ops));
		selector.wakeup();
	}

	static class RegistrationRequest
	{
		final SelectableChannel channel;
		final PollEvents handler;
		final int ops;

		public RegistrationRequest(SelectableChannel channel,
				PollEvents handler, int ops)
		{
			this.channel = channel;
			this.handler = handler;
			this.ops = ops;
		}

	}
	ConcurrentLinkedQueue<RegistrationRequest> registrations = new ConcurrentLinkedQueue<RegistrationRequest>();

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.Poller#modify(java.nio.channels.SelectionKey, int)
	 */
	public void modify(SelectionKey key, int ops) {
		key.interestOps(ops);
		selector.wakeup();
	}

	/* (non-Javadoc)
	 * @see com.cosylab.jam.io.Poller#remove(java.nio.channels.SelectionKey)
	 */
	public void remove(SelectionKey key) {
		key.cancel();
	}

	public void pollOne() throws IOException
	{
		while (true)
		{
			RegistrationRequest rr = registrations.poll();
			if (rr == null)
				break;

			try
			{
				SelectionKey key = rr.channel.register(selector, rr.ops, rr.handler);
				rr.handler.registeredNotify(key, null);
			} catch (Throwable registrationException) {
				// watch for exception from here
				rr.handler.registeredNotify(null, registrationException);
			}
		}

		int numSelectedKeys;
		if (trottle == 0)
		{
			numSelectedKeys = selector.select();
		}
		else
		{
			numSelectedKeys = selector.selectNow();
			trottle--;
		}

		//System.out.println("polled with:" + numSelectedKeys);

		if (numSelectedKeys == 0)
			return;

		Iterator<SelectionKey> selectedKeysIterator = selector.selectedKeys().iterator();

		while (selectedKeysIterator.hasNext())
		{
			SelectionKey key = selectedKeysIterator.next();

			selectedKeysIterator.remove();

			PollEvents pollEvents = (PollEvents)key.attachment();

			// callback guard
			try
			{
				pollEvents.pollNotify(key);
			} catch (CancelledKeyException cke) {
				// noop
			}
		}

		trottle = 5;	// TODO tune
	}

	public void run()
	{
		try
		{
			// TODO shutdown
			while (true)
				pollOne();
		} catch (Throwable th) {
			// IOException, ClosedSelectorException
			// and any others...
			// TODO
			th.printStackTrace();
		}
	}

}
