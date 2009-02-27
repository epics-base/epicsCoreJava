/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import org.epics.pvData.property.PVProperty;
import org.epics.pvData.property.PVPropertyFactory;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVListener;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;


/**
 * @author mrk
 *
 */
public class PVListenerForTesting implements PVListener{ 
    private static PVProperty pvProperty = PVPropertyFactory.getPVProperty();
    private String recordName = null;
    private String pvName = null;
    private boolean monitorProperties = false;
    private boolean verbose;
    private PVRecord pvRecord = null;
    
    private String actualFieldName = null;
    private boolean isGroupPut = false;
    private String fullName = null;
    
    /**
     * Constructor
     * @param pvDatabase The database.
     * @param recordName The name of the record.
     * @param pvName The name of the field to monitor.
     * @param monitorProperties
     * @param verbose
     */
    public PVListenerForTesting(PVDatabase pvDatabase,String recordName,String pvName,
        boolean monitorProperties,boolean verbose)
    {
        this.pvName = pvName;
        this.verbose = verbose;
        this.recordName = recordName;
        this.monitorProperties = monitorProperties;
        pvRecord = pvDatabase.findRecord(recordName);
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        this.pvName = pvName;
        connect();
    }
    
    /**
     * Another constructor.
     * @param pvDatabase The database.
     * @param recordName The name of the record.
     * @param pvName The name of the field to monitor.
     */
    public PVListenerForTesting(PVDatabase pvDatabase,String recordName,String pvName)
    {
        this(pvDatabase,recordName,pvName,true,true);
    }
    
    private String putCommon(String message) {
        if(!verbose) {
            return fullName + " ";
        }
        return String.format("%s %s isGroupPut %b pvName %s actualFieldName %s%n",
            message,
            fullName,
            isGroupPut,
            pvName,
            actualFieldName);
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#beginGroupPut(org.epics.pvData.pv.PVRecord)
     */
    public void beginGroupPut(PVRecord pvRecord) {
        isGroupPut = true;
        putCommon("beginProcess");
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#endGroupPut(org.epics.pvData.pv.PVRecord)
     */
    public void endGroupPut(PVRecord pvRecord) {
        putCommon("endProcess");
        isGroupPut = false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#dataPut(org.epics.pvData.pv.PVField)
     */
    public void dataPut(PVField pvField) {
    	String message = "dataput ";
    	String name = pvField.getFullName();
        if(!name.equals(fullName)) {
            message  += "to property of ";
        } else {
        	message += "to ";
        }
        String common = putCommon(message);
        System.out.printf("%s    %s = %s%n",
            common,name,pvField.toString(2));
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#dataPut(org.epics.pvData.pv.PVStructure, org.epics.pvData.pv.PVField)
     */
    public void dataPut(PVStructure pvRequested, PVField pvField) {
        String structureName = pvRequested.getFullName();
        String common = putCommon(structureName +" dataPut to field " + pvField.getFullFieldName());
        System.out.printf("%s    = %s%n",common,pvField.toString(2));
    }       
    /* (non-Javadoc)
     * @see org.epics.pvData.pv.PVListener#unlisten(org.epics.pvData.pv.PVRecord)
     */
    public void unlisten(PVRecord pvRecord) {
        connect();
    }
    
    private void connect() {
        if(pvRecord==null) {
            System.out.printf("record %s not found%n",recordName);
            return;
        }
        PVField pvField;
        if(pvName==null || pvName.length()==0) {
            pvField = pvRecord;
        } else {
            pvField = pvRecord.getSubField(pvName);
            if(pvField==null){
                System.out.printf("name %s not in record %s%n",pvName,recordName);
                System.out.printf("%s\n",pvRecord.toString());
                return;
            }
        }
        actualFieldName = pvField.getField().getFieldName();
        fullName = pvField.getFullName();
        pvRecord.registerListener(this);
        pvField.addListener(this);
        if(monitorProperties) {
            String[] propertyNames = pvProperty.getPropertyNames(pvField);
            if(propertyNames!=null) {
                for(String propertyName : propertyNames) {
                    PVField pvf = pvProperty.findProperty(pvField, propertyName);
                   pvf.addListener(this);
                }
            }
        }
    }
}
