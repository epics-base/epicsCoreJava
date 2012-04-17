package org.epics.pvaccess.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author msekoranja
 */
public class Mailbox<E> {

	private final ConcurrentLinkedQueue<E> queue = new ConcurrentLinkedQueue<E>();
	private final AtomicInteger elements = new AtomicInteger(0);
	private final AtomicBoolean wakeup = new AtomicBoolean(false);

	public void put(E msg)
	{
		queue.add(msg);
		if (elements.incrementAndGet() == 1)
		{
		    synchronized (elements)
		    {
		        elements.notify();
		    }
		}
	}
	
	public E take(long timeout) throws InterruptedException
	{
		while (true)
		{
			E val = queue.poll();
			if (val == null)
			{
			    synchronized (elements)
			    {
			    	boolean isEmpty = queue.isEmpty();
			    	if (isEmpty && timeout < 0)
			    		return null;
			    	
			        while (isEmpty)
			        {
			        	elements.wait(timeout);		
			        	isEmpty = queue.isEmpty();
			        	if (isEmpty)
			        	{
			        		if (timeout > 0)	// TODO spurious wakeup, but not critical
			        			return null;
			        		else // if (timeout == 0)	cannot be negative
			        		{
			        			if (wakeup.getAndSet(false))
			        				return null;
			        		}
			        	}
			        }
			    }
			}
			else
			{
				elements.decrementAndGet();
				return val;
			}
		}
	}

	// NOTE: size is O(n)
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}
	
	public void clear()
	{
		queue.clear();
	}
	
	public void wakeup()
	{
		if (!wakeup.getAndSet(true))
		{
			synchronized (elements)
			{
				elements.notifyAll();
			}
		}
	}
}
