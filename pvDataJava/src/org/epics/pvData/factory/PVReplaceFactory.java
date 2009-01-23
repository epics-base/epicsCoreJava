/**
 * 
 */
package org.epics.pvData.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

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
 * @author mrk
 *
 */
public class PVReplaceFactory {
    public static void replace(PVDatabase pvDatabase) {
        for(PVRecord pvRecord: pvDatabase.getRecords()) {
            replace(pvDatabase,pvRecord);
        }
    }
    
    public static void replace(PVDatabase pvDatabase,PVRecord pvRecord) {
       replace(pvDatabase,pvRecord.getPVFields());
    }
    
    private static void replace(PVDatabase pvDatabase,PVField[] pvFields) {
        for(PVField pvField : pvFields) {
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
                replace(pvField,factory);
                break;
            }
            if(pvField.getField().getType()==org.epics.pvData.pv.Type.structure) {
                PVStructure pvStructure = (PVStructure)pvField;
                replace(pvDatabase,pvStructure.getPVFields());
            }
        }
    }
    
    private static void replace(PVField pvField,PVStructure factory) {
        PVString pvString = factory.getStringField("pvReplaceFactory");
        if(pvString==null) {
            pvField.message("PVReplaceFactory structure " + factory.getFullName() + " is not a pvReplaceFactory", MessageType.error);
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
