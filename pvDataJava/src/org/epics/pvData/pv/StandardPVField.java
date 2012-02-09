/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvData.pv;

/**
 * @author mrk
 *
 */
public interface StandardPVField {
    /**
     * @param parent
     * @param fieldName
     * @param type
     * @return
     */
    PVScalar scalar(PVStructure parent,String fieldName,ScalarType type);
    /**
     * @param parent
     * @param fieldName
     * @param type
     * @param properties
     * @return
     */
    PVStructure scalar(PVStructure parent,String fieldName,ScalarType type,String properties);
    /**
     * @param parent
     * @param fieldName
     * @param elementType
     * @return
     */
    PVScalarArray scalarArray(PVStructure parent,String fieldName,ScalarType elementType);
    /**
     * @param parent
     * @param fieldName
     * @param elementType
     * @param properties
     * @return
     */
    PVStructure scalarArray(PVStructure parent,String fieldName,ScalarType elementType, String properties);
    /**
     * @param parent
     * @param fieldName
     * @param structure
     * @return
     */
    PVStructureArray structureArray(PVStructure parent,String fieldName,Structure structure);
    /**
     * @param parent
     * @param fieldName
     * @param structure
     * @param properties
     * @return
     */
    PVStructure structureArray(PVStructure parent,String fieldName,Structure structure,String properties);
    /**
     * @param parent
     * @param fieldName
     * @param choices
     * @param number
     * @return
     */
    PVStructure enumerated(PVStructure parent,String fieldName,String[] choices, int number);
    /**
     * @param parent
     * @param fieldName
     * @param choices
     * @param number
     * @param properties
     * @return
     */
    PVStructure enumerated(PVStructure parent,String fieldName,String[] choices, int number, String properties);
    /**
     * @param parent
     * @param type
     * @return
     */
    PVScalar scalarValue(PVStructure parent,ScalarType type);
    /**
     * @param parent
     * @param type
     * @param properties
     * @return
     */
    PVStructure scalarValue(PVStructure parent,ScalarType type,String properties);
    /**
     * @param parent
     * @param elementType
     * @return
     */
    PVScalarArray scalarArrayValue(PVStructure parent,ScalarType elementType);
    /**
     * @param parent
     * @param elementType
     * @param properties
     * @return
     */
    PVStructure scalarArrayValue(PVStructure parent,ScalarType elementType, String properties);
    /**
     * @param parent
     * @param structure
     * @return
     */
    PVStructureArray structureArrayValue(PVStructure parent,Structure structure);
    /**
     * @param parent
     * @param structure
     * @param properties
     * @return
     */
    PVStructure structureArrayValue(PVStructure parent,Structure structure,String properties);
    /**
     * @param parent
     * @param choices
     * @param number
     * @return
     */
    PVStructure enumeratedValue(PVStructure parent,String[] choices,int number);
    /**
     * @param parent
     * @param choices
     * @param number
     * @param properties
     * @return
     */
    PVStructure enumeratedValue(PVStructure parent,String[] choices,int number, String properties);
    /**
     * @param parent
     * @return
     */
    PVStructure alarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure timeStamp(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure display(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure control(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure booleanAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure byteAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure shortAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure intAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure longAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure floatAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure doubleAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure enumeratedAlarm(PVStructure parent);
    /**
     * @param parent
     * @return
     */
    PVStructure powerSupply(PVStructure parent);

}
