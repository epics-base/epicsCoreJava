/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca.types;


import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.datasource.DataSourceTypeAdapter;
import org.epics.gpclient.datasource.ca.CAConnectionPayload;
import org.epics.gpclient.datasource.ca.CAMessagePayload;

abstract public class CATypeAdapter implements DataSourceTypeAdapter<CAConnectionPayload, CAMessagePayload> {

    private final Class<?> typeClass;
    private final DBRType epicsValueType;
    private final DBRType epicsMetaType;
    private final Boolean array;

    /**
     * Creates a new type adapter.
     *
     * @param typeClass the java type this adapter will create
     * @param epicsValueType the epics type used for the monitor
     * @param epicsMetaType the epics type for the get at connection time; null if no metadata is needed
     * @param array true whether this will require an array type
     */
    public CATypeAdapter(Class<?> typeClass, DBRType epicsValueType, DBRType epicsMetaType, Boolean array) {
        this.typeClass = typeClass;
        this.epicsValueType = epicsValueType;
        this.epicsMetaType = epicsMetaType;
        this.array = array;
    }


    public boolean match(ReadCollector<?, ?> cache, CAConnectionPayload connectionPayload) {

        Channel channel = connectionPayload.getChannel();

        // If the generated type can't be put in the cache, no match
        if (!cache.getType().isAssignableFrom(typeClass))
            return false;

        // If the type of the channel does not match, no match
        if (!dbrTypeMatch(epicsValueType, connectionPayload.getFieldType()))
            return false;

        // If processes array, but count is 1, no match
        if (array != null && array && channel.getElementCount() == 1)
            return false;

        // If processes scalar, but the count is not 1, no match
        if (array != null && !array && channel.getElementCount() != 1)
            return false;

        // Everything matches
        return true;
    }
    private static boolean dbrTypeMatch(DBRType aType, DBRType anotherType) {
        if (aType.getClass() == null && anotherType.getClass() != null) {
            return false;
        }
        if (aType.getClass() != null && anotherType.getClass() == null) {
            return false;
        }
        return aType.isBYTE() && anotherType.isBYTE() ||
                aType.isDOUBLE() && anotherType.isDOUBLE() ||
                aType.isENUM() && anotherType.isENUM() ||
                aType.isFLOAT() && anotherType.isFLOAT() ||
                aType.isINT() && anotherType.isINT() ||
                aType.isSHORT() && anotherType.isSHORT() ||
                aType.isSTRING() && anotherType.isSTRING();
    }
    public Object getSubscriptionParameter(ReadCollector<?, ?> cache, CAConnectionPayload connection) {
        throw new UnsupportedOperationException("Not implemented: CAChannelHandler is multiplexed, will not use this method");

    }

    public void updateCache(ReadCollector cache, CAConnectionPayload connection, CAMessagePayload message) {
        Channel channel = connection.getChannel();
        // If metadata is required and not present, no update
        if (epicsMetaType != null && message.getMetadata() == null)
            return;
        // If value is not present, no update
        if (message.getEvent() == null)
            return;
        cache.updateValue(createValue(message.getEvent().getDBR(), message.getMetadata(), connection));
        return;
    }

    /**
     * Given the value create the new value.
     *
     * @param message the value taken from the monitor
     * @param metadata the value field metadata, optional
     * @param connPayload connection playload
     *
     * @return the new value
     */
    public abstract Object createValue(DBR message, DBR metadata, CAConnectionPayload connPayload);

}
