/**
 * 
 */
package org.epics.pvaccess.client.pvds;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.epics.pvaccess.client.pvds.Protocol.GUID;
import org.epics.pvaccess.client.pvds.util.BloomFilter;
import org.epics.pvaccess.client.pvds.util.ToByteArraySerializator;

/**
 * @author msekoranja
 *
 */
public class DiscoveryServiceImpl<T> extends TimerTask {

	private final long announcePeriod;
	private final long minAnnouncePeriod;
	private final DiscoveryDataSet<T> dataSet;
	private final ToByteArraySerializator<T> serializator;
	
	private BloomFilter<T> filter;
	private short count;		// change count
	
	private final Timer timer = new Timer("DiscoveryServiceImpl timer");
	private long lastAnnounceTime;
	
	public DiscoveryServiceImpl(
			long announcePeriod, long minAnnouncePeriod,
			GUID guid,
			DiscoveryDataSet<T> dataSet,
			ToByteArraySerializator<T> serializator)
	{
		if (announcePeriod < minAnnouncePeriod)
			throw new IllegalArgumentException("announcePeriod < minAnnouncePeriod");
		
		this.announcePeriod = announcePeriod;
		this.minAnnouncePeriod = minAnnouncePeriod;
		this.dataSet = dataSet;
		this.serializator = serializator;
		refreshEntitiesNotify(dataSet.getEntities());
		
		timer.schedule(this, this.announcePeriod, this.announcePeriod);
	}
	
	/*
	// periodically (30s?) sends out announce message on multicast
	// (GUID, unicast discovery endpoint, service endpoint(s), changed status id, 
	//  # discoverable entities (-1 for explicit discovery only), bloom filter 

	// subscribers announce themselves too, with changed status id
	// publishers send to unicast discovery address their annoincements (reliable)

	// this implies that all the entities are available on shared enpoints,
	// i.e. no separate enpoints per entity (an entity must become a discovery service in this case)
	
	// discovery enpoint: IPv6 address + port
	// enpoint: string protocol, address size, <data> 	- this also supports local, shmem, etc.
	
	// bloom filter = { k, m, BitSet }
	
	
	// an admin GUI can get list of active (connected) entities from the publishers/subscribers
	// via unicast discovery port: ( endpoint, entity[]  )[]
*/	
	// sends out updated announce multicast, rate limited
	// send is scheduled after rate limit period
	// contains only added entities, at the time of a call getEntities() must already be updated
	public void addedEntitiesNotify(Set<T> entities)
	{
		// TODO just add or resize to?
		add2Filter(entities);
		
		announce();
	}
	
	// sends out updated announce multicast, rate limited
	// send is scheduled after rate limit period
	// contains only added entities, at the time of a call getEntities() must already be updated
	public void removedEntitiesNotify(Set<T> entities)
	{
		// bloom filter does not support removal (not a count type bloom filter)
		// trigger refresh
		refreshEntitiesNotify(dataSet.getEntities());
	}

	// sends out updated announce multicast, rate limited
	// send is scheduled after rate limit period
	// contains all the entities, entities == getEntities(), will reconstruct filter
	public void refreshEntitiesNotify(Set<T> entities)
	{
		// TODO
		double p = 0.01;
		int minFilterSize = 1024;	// defaults to 0
		int maxFilterSize = 10240;	// defaults to Integer.MAX_VALUE
		
		int n = Math.max(minFilterSize, entities.size());
		n = Math.min(maxFilterSize, n);
		
		filter = new BloomFilter<T>(serializator, p, n);
		add2Filter(entities);

		announce();
	}

	private void add2Filter(Set<T> entities)
	{
		count++;		
		for (T e : entities)
			filter.add(e);
	}
	
	@Override
	public void run() {
		announce();
	}

	private void announce()
	{
		long now = System.currentTimeMillis();
		//long diff = now - lastAnnounceTime; 
		
		System.out.println(filter);

		lastAnnounceTime = now;
		
		//timer.schedule(this, announcePeriod);
	}
}