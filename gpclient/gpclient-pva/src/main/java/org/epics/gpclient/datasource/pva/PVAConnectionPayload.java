/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.pva;

import java.util.LinkedHashMap;
import java.util.Map;
import org.epics.pvdata.pv.Field;

/**
 *
 * @author carcassi
 */
class PVAConnectionPayload {
    final Field channelType;
    final boolean connected;
    final String extractFieldName;

    public PVAConnectionPayload(Field channelType, boolean connected, String extractFieldName) {
        this.channelType = channelType;
        this.connected = connected;
        this.extractFieldName = extractFieldName;
    }

    @Override
    public String toString() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("connected", connected);
        properties.put("channelType", channelType);
        properties.put("extractFieldName", extractFieldName);
        return properties.toString();
    }
    
}
