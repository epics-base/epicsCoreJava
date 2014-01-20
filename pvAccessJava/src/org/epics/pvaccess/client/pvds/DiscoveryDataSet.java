/**
 * 
 */
package org.epics.pvaccess.client.pvds;

import java.util.Set;

/**
 * @author msekoranja
 *
 */
public interface DiscoveryDataSet<T> {

	/**
	 * Get list of discoverable entities.
	 * @return list of discoverable entities,
	 * 			<code>null</code> if service supports only explicit discovery via <code>hasEntiry(T)<code>.
	 */
	Set<T> getEntities(); 
	
	/**
	 * Explicit discovery check.
	 * @param entity to be checked for existence.
	 * @return <code>true</code> if entity exists, <code>false</code> otherwise
	 */
	boolean hasEntity(T entity);
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
	
	// sends out updated announce multicast, rate limited
	// send is scheduled after rate limit period
	void addEntities(List<T> entities);
	
	// sends out updated announce multicast, rate limited
	// send is scheduled after rate limit period
	void removeEntities(List<T> entities);
*/