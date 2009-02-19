package org.epics.pvData.factory;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.ScalarType;

/**
 * Base Class for PVAuxInfo. Normally this is not extended.
 * @author mrk
 *
 */
public class BasePVAuxInfo implements PVAuxInfo {
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private TreeMap<String,PVScalar> attributeMap = new TreeMap<String,PVScalar>();
    
    private PVField pvField;
    /**
     * Constructor for BaseFieldAttribute.
     */
    public  BasePVAuxInfo(PVField pvField){
        this.pvField = pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVAuxInfo#getPVField()
     */
    public PVField getPVField() {
        return pvField;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVAuxInfo#getInfo(java.lang.String)
     */
    public synchronized PVScalar getInfo(String key) {
        return attributeMap.get(key);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVAuxInfo#getInfos()
     */
    public synchronized Map<String, PVScalar> getInfos() {
        return attributeMap;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVAuxInfo#createInfo(java.lang.String, org.epics.pvData.pv.ScalarType)
     */
    public synchronized PVScalar createInfo(String key,ScalarType scalarType) {
        PVScalar pvScalar = pvDataCreate.createPVScalar(null,key,scalarType);
        PVScalar old = attributeMap.put(key, pvScalar);
        if(old!=null) return old;
        return pvScalar;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString(0);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVAuxInfo#toString(int)
     */
    public synchronized String toString(int indentLevel) {
        String result = "";
        Set<String> keys = attributeMap.keySet();
        for(String key : keys) {
            result += " " + key + "=" + attributeMap.get(key);
        }
        return result;
    }
}