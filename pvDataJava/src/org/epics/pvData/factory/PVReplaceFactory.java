/**
 * 
 */
package org.epics.pvData.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.ScalarType;

/**
 * Factory that looks for and calls factories that replace the default implementation of a field.
 * @author mrk
 *
 */
public class PVReplaceFactory {
    
    /**
     * Look at every field of every record in the database and see if field implementation should be replaced.
     * @param pvDatabase The database.
     */
    public static void replace(PVDatabase pvDatabase) {
        for(PVRecord pvRecord: pvDatabase.getRecords()) {
            replace(pvDatabase,pvRecord.getPVStructure());
        }
    }
    
    /**
     * Look at every field of pvStructure and see if the field implementation should be replaced.
     * @param pvDatabase The database to look for pvReplaceFactorys
     * @param pvStructure The pvStructure
     */
    public static void replace(PVDatabase pvDatabase,PVStructure pvStructure) {
       replace(pvDatabase,pvStructure.getPVFields());
    }
    
    /**
     * Look at the field and see if the field implementation should be replaced.
     * If it is a structure field also look at the subfields.
     * @param pvDatabase The database to look for pvReplaceFactorys.
     * @param pvField The field.
     */
    public static void replace(PVDatabase pvDatabase,PVField pvField) {
        PVAuxInfo pvAuxInfo = pvField.getPVAuxInfo();
        PVScalar pvScalar = pvAuxInfo.getInfo("pvReplaceFactory");
        while(pvScalar!=null) {
            if(pvScalar.getScalar().getScalarType()!=ScalarType.pvString) {
                pvField.message("PVReplaceFactory: pvScalar " + pvScalar.getFullName() + " is not a string", MessageType.error);
                break;
            }
            String factoryName = ((PVString)pvScalar).get();
            PVStructure factory = pvDatabase.findStructure(factoryName);
            if(factory==null) {
                pvField.message("PVReplaceFactory: factory " + factoryName + " not found", MessageType.error);
                break;
            }
            String fieldName = pvField.getField().getFieldName();
            PVStructure pvParent = pvField.getParent();
            replace(pvField,factory);
            pvField = pvParent.getSubField(fieldName);
            pvAuxInfo = pvField.getPVAuxInfo();
            PVScalar pvNew = pvAuxInfo.createInfo("pvReplaceFactory", pvScalar.getScalar().getScalarType());
            convert.copyScalar(pvScalar, pvNew);
            break;
        }
        if(pvField.getField().getType()==org.epics.pvData.pv.Type.structure) {
            PVStructure pvStructure = (PVStructure)pvField;
            replace(pvDatabase,pvStructure.getPVFields());
        }
    }
    
    private static final Convert convert = ConvertFactory.getConvert();
    private static void replace(PVDatabase pvDatabase,PVField[] pvFields) {
        for(PVField pvField : pvFields) {
            replace(pvDatabase,pvField);
        }
    }
    
    
    
    private static void replace(PVField pvField,PVStructure factory) {
        PVString pvString = factory.getStringField("pvReplaceFactory");
        if(pvString==null) {
            pvField.message("PVReplaceFactory structure " + factory.getFullName() + " is not a pvReplaceFactory", MessageType.error);
            return;
        }
        String factoryName = pvString.get();
        Class supportClass;
        Method method = null;
        try {
            supportClass = Class.forName(factoryName);
        }catch (ClassNotFoundException e) {
           pvField.message("PVReplaceFactory ClassNotFoundException factory " + factoryName 
            + " " + e.getLocalizedMessage(),MessageType.error);
           return;
        }
        try {
            method = supportClass.getDeclaredMethod("replacePVField",
                    Class.forName("org.epics.pvData.pv.PVField"));
            
        } catch (NoSuchMethodException e) {
            pvField.message("PVReplaceFactory NoSuchMethodException factory " + factoryName 
                    + " " + e.getLocalizedMessage(),MessageType.error);
                    return;
        } catch (ClassNotFoundException e) {
            pvField.message("PVReplaceFactory ClassNotFoundException factory " + factoryName 
            + " " + e.getLocalizedMessage(),MessageType.error);
            return;
        }
        if(!Modifier.isStatic(method.getModifiers())) {
            pvField.message("PVReplaceFactory factory " + factoryName 
            + " create is not a static method ",MessageType.error);
            return;
        }
        try {
            method.invoke(null,pvField);
            return;
        } catch(IllegalAccessException e) {
            pvField.message("PVReplaceFactory IllegalAccessException factory " + factoryName 
            + " " + e.getLocalizedMessage(),MessageType.error);
            return;
        } catch(IllegalArgumentException e) {
            pvField.message("PVReplaceFactory IllegalArgumentException factory " + factoryName 
            + " " + e.getLocalizedMessage(),MessageType.error);
            return;
        } catch(InvocationTargetException e) {
            pvField.message("PVReplaceFactory InvocationTargetException factory " + factoryName 
            + " " + e.getLocalizedMessage(),MessageType.error);
        }
    }
}
