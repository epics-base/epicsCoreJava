/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvdata.factory;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVAuxInfo;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.ScalarType;

/**
 * Base Class for PVAuxInfo. Normally this is not extended.
 * @deprecated
 * This is used by pvIOCJava.
 * It should be changed so that this is no longer required.
 * @author mrk
 *
 */
public class BasePVAuxInfo implements PVAuxInfo {
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static Convert convert = ConvertFactory.getConvert();
    private TreeMap<String,PVScalar> attributeMap = new TreeMap<String,PVScalar>();
    
    private PVField pvField;
    /**
     * Constructor
     */
    public  BasePVAuxInfo(PVField pvField){
        this.pvField = pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVAuxInfo#getPVField()
     */
    public PVField getPVField() {
        return pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVAuxInfo#getInfo(java.lang.String)
     */
    public synchronized PVScalar getInfo(String key) {
        return attributeMap.get(key);
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVAuxInfo#getInfos()
     */
    public synchronized Map<String, PVScalar> getInfos() {
        return attributeMap;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVAuxInfo#createInfo(java.lang.String, org.epics.pvdata.pv.ScalarType)
     */
    public synchronized PVScalar createInfo(String key,ScalarType scalarType) {
        PVScalar old = attributeMap.get(key);
        if(old!=null) {
            ScalarType oldType = old.getScalar().getScalarType();
            if(oldType==scalarType) return old;
            String message = "AuxoInfo:create key " + key + " already exists with scalarType " + oldType.toString();
            message += " requestType is " + scalarType.toString();
            throw new IllegalArgumentException(message);
        }
        PVScalar pvScalar = pvDataCreate.createPVScalar(scalarType);
        attributeMap.put(key, pvScalar);
        return pvScalar;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVAuxInfo#toString(java.lang.StringBuilder, int)
     */
    @Override
    public void toString(StringBuilder buf, int indentLevel) {
        if(attributeMap.isEmpty()) return;
        convert.newLine(buf,indentLevel);
        buf.append("auxinfo");
        Set<Map.Entry<String, PVScalar>> set = attributeMap.entrySet();
        for(Map.Entry<String,PVScalar> entry : set) {
             convert.newLine(buf,indentLevel+1);
             String key = entry.getKey();
             PVScalar value = attributeMap.get(key);
             value.toString(buf,indentLevel+1);
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.pv.PVAuxInfo#toString(java.lang.StringBuilder)
     */
    @Override
    public void toString(StringBuilder buf) {
        toString(buf,0);
    }
}
