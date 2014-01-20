/**
 * 
 */
package org.epics.pvaccess.client.pvds;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.epics.pvaccess.client.pvds.Protocol.EntityId;
import org.epics.pvaccess.client.pvds.Protocol.GUID;
import org.epics.pvaccess.client.pvds.Protocol.GUIDPrefix;
import org.epics.pvaccess.client.pvds.util.StringToByteArraySerializator;

/**
 * @author msekoranja
 *
 */
public class TestMain {
	
	// one per process
	public static final GUIDPrefix GUID_PREFIX = GUIDPrefix.generateGUIDPrefix();
	public static final AtomicInteger participandId = new AtomicInteger();
	
	public static void main(String[] args)
	{
		final HashSet<String> entities = new HashSet<String>();
		for (int i = 0; i < 1000; i++)
			entities.add("test" + String.valueOf(i));
		
		DiscoveryDataSet<String> dataSet = new DiscoveryDataSet<String>()
		{
			@Override
			public Set<String> getEntities() {
				return entities;
			}

			@Override
			public boolean hasEntity(String entity) {
				return entities.contains(entities);
			}
		};
		
		//DiscoveryServiceImpl<String> ds = 
			new DiscoveryServiceImpl<String>(
					30*1000,
					1*1000,
					new GUID(GUID_PREFIX, EntityId.generateParticipantEntityId(participandId.incrementAndGet())),
					dataSet,
					StringToByteArraySerializator.INSTANCE
				);
	}
}