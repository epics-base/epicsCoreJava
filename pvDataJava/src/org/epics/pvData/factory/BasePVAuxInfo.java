package org.epics.pvData.factory;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.MessageType;
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
    private static Convert convert = ConvertFactory.getConvert();
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
        PVScalar old = attributeMap.get(key);
        if(old!=null) {
            ScalarType oldType = old.getScalar().getScalarType();
            if(oldType==scalarType) return old;
            pvField.message("AuxoInfo:create key " + key + " already exists with scalarType " + oldType.toString()
                    + " requestType is " + scalarType.toString(),MessageType.error);
            return null;
        }
        PVScalar pvScalar = pvDataCreate.createPVScalar(null,key,scalarType);
        attributeMap.put(key, pvScalar);
        return pvScalar;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVAuxInfo#toString(java.lang.StringBuilder, int)
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
     * @see org.epics.pvData.pv.PVAuxInfo#toString(java.lang.StringBuilder)
     */
    @Override
    public void toString(StringBuilder buf) {
        toString(buf,0);
    }
}