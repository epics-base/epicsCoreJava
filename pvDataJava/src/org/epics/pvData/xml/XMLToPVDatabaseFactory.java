/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.factory.PVDatabaseFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.Type;


/**
 * Factory to convert an xml file to an IOCDatabase and put it in the database.
 * The only public method is convert.
 * @author mrk
 *
 */
public class XMLToPVDatabaseFactory {
    private static PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static AtomicBoolean isInUse = new AtomicBoolean(false);
    //  for use by private classes
    private static Convert convert = ConvertFactory.getConvert();
    private static Pattern primitivePattern = Pattern.compile("[, ]");
    private static Pattern stringPattern = Pattern.compile("\\s*,\\s*");
    private static PVDatabase pvDatabase;
    private static Requester requester;
    private static PVXMLReader iocxmlReader;
    /**
     * Convert an xml file to PVDatabase definitions and put the definitions in a database.
     * @param pvDatabase The database into which the new structure and records are added.
     * @param fileName The name of the file containing xml record instance definitions.
     * @param requester The requester.
     */
    public static void convert(PVDatabase pvDatabase, String fileName,Requester requester)
    {
        boolean gotIt = isInUse.compareAndSet(false,true);
        if(!gotIt) {
            requester.message("XMLToIOCDBFactory.convert is already active",MessageType.fatalError);
        }
        try {
            XMLToPVDatabaseFactory.pvDatabase = pvDatabase;
            XMLToPVDatabaseFactory.requester = requester;
            PVXMLListener listener = new Listener();
            iocxmlReader = PVXMLReaderFactory.getReader();
            iocxmlReader.parse("database",fileName,listener);
        } finally {
            isInUse.set(false);
        }
        
    }
    
    /**
     * Create an IOC Database (IOCDB) and populate it
     * with definitions from an XML record instance.
     * @param databaseName The name for the PVDatabase.
     * The definitions are not added to the master IOCDB but the caller can call IOCDB.mergeIntoMaster
     * to add them to master.
     * Attempting to add definitions for a record instance that is already in master is an error.
     * @param fileName The file containing record instances definitions.
     * @param requester A listener for error messages.
     * @return An IOC Database that has the newly created record instances.
     */
    public static PVDatabase convert(String databaseName,String fileName,Requester requester) {
        boolean gotIt = isInUse.compareAndSet(false,true);
        if(!gotIt) {
            requester.message("XMLToIOCDBFactory is already active", MessageType.error);
            return null;
        }
        try {
            XMLToPVDatabaseFactory.pvDatabase = PVDatabaseFactory.create(databaseName);           
            XMLToPVDatabaseFactory.requester = requester;
            PVXMLListener listener = new Listener();
            iocxmlReader = PVXMLReaderFactory.getReader();
            iocxmlReader.parse("PVDatabase",fileName,listener);
            return XMLToPVDatabaseFactory.pvDatabase;
        } finally {
            isInUse.set(false);
        }
    }
    
    private static class Listener implements PVXMLListener
    {
        private enum State {
            idle,
            record,
            structure,
            scalar,
            scalarArray,
            auxInfo
        } 
        private State state = State.idle;
        
        private static class StructureState {
            private State prevState = null;
            private PVStructure pvStructure;
        }
        private Stack<StructureState> structureStack = new Stack<StructureState>();
        private StructureState structureState = null;
        
        private PVScalar pvScalar = null;
        private StringBuilder scalarBuilder = new StringBuilder();
        private State scalarPrevState = null;
        
        private PVArray pvArray = null;
        private StringBuilder arrayBuilder = new StringBuilder();
        private State arrayPrevState = null;
        private int capacity = 0;
        private int length = 0;
        private int offset = 0;
        private boolean capacityMutable = true;
        
        private String auxInfoName = null;
        private ScalarType auxInfoType = null;
        private StringBuilder auxInfoBuilder = new StringBuilder();
        private State auxInfoPrevState = null;
 
       
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLListener#endDocument()
         */
        public void endDocument() {}       
       
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLListener#message(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        public void message(String message,MessageType messageType) {
            requester.message(message, messageType);
        }
       
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLListener#startElement(java.lang.String, java.util.Map)
         */
        public void startElement(String qName,Map<String,String> attributes)
        {
            switch(state) {
            case idle:
                if(qName.equals("structure")) {
                    startStructure(qName,attributes);
                } else if(qName.equals("record")) {
                    startRecord(qName,attributes);
                } else {
                    iocxmlReader.message(
                        "startElement " + qName + " not understood currect state is idle",
                        MessageType.error);
                }
                return;
            case record:
            case structure:
                if(qName.equals("structure")) {
                    startStructure(qName,attributes);
                } else if(qName.equals("scalar")) {
                    startScalar(qName,attributes);
                } else if(qName.equals("array")) {
                    startScalarArray(qName,attributes);
                } else if(qName.equals("auxInfo")) {
                    startAttribute(qName,attributes);
                } else {
                    iocxmlReader.message(
                            "startElement " + qName + " not understood current state is structure",
                            MessageType.error);
                }
                return;
            case scalar:
                if(qName.equals("auxInfo")) {
                    startAttribute(qName,attributes);
                } else {
                    iocxmlReader.message(
                            "startElement " + qName + " not understood current state is scalar",
                            MessageType.error);
                }
                return;
            case scalarArray:
                if(qName.equals("auxInfo")) {
                    startAttribute(qName,attributes);
                } else {
                    iocxmlReader.message(
                            "startElement " + qName + " not understood current state is scalarArray",
                            MessageType.error);
                }
                return;
            case auxInfo:
                iocxmlReader.message(
                        "startElement " + qName + " not understood current state is auxInfo",
                        MessageType.error);
                return;
            default:
                iocxmlReader.message(
                        "startElement " + qName + " Logic Error in parser",
                        MessageType.error);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLListener#endElement(java.lang.String)
         */
        public void endElement(String qName)
        {
            switch(state) {
            case idle:
                return;
            case record:
                if(qName.equals("record")) {
                    endRecord(qName);
                } else {
                    iocxmlReader.message(
                            "endElement " + qName + " not understood",
                            MessageType.error);
                }
                return;
            case structure:
                if(qName.equals("structure")) {
                    endStructure(qName);
                } else {
                    iocxmlReader.message(
                            "endElement " + qName + " not understood",
                            MessageType.error);
                }
                return;
            case scalar:
                if(qName.equals("scalar")) {
                    endScalar(qName);
                } else {
                    iocxmlReader.message(
                            "endElement " + qName + " not understood",
                            MessageType.error);
                }
                return;
            case scalarArray:
                if(qName.equals("array")) {
                    endScalarArray(qName);
                } else {
                    iocxmlReader.message(
                            "endElement " + qName + " not understood",
                            MessageType.error);
                }
                return;
            case auxInfo:
                if(qName.equals("auxInfo")) {
                    endAttribute(qName);
                } else {
                    iocxmlReader.message(
                        "endElement " + qName + " not understood",
                        MessageType.error);
                }
                return;
            default:
                iocxmlReader.message(
                        "endElement " + qName + " Logic Error in parser",
                        MessageType.error);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.PVXMLListener#characters(char[], int, int)
         */
        public void characters(char[] ch, int start, int length)
        {
            switch(state) {
            case idle:
                return;
            case structure:
                return;
            case scalar:
                charactersScalar(ch,start,length);
                return;
            case scalarArray:
                charactersScalarArray(ch,start,length);
            case auxInfo:
                charactersAttribute(ch,start,length);
                return;
            default:
                return;
            }
        }
        
        private void startRecord(String qName,Map<String,String> attributes)
        {
            if(state!=State.idle) {
                iocxmlReader.message(
                        "startElement " + qName + " not allowed except as top level structure",
                        MessageType.error);
                return;
            }
            if(structureStack.size()!=0) {
                iocxmlReader.message(
                        "startElement " + qName + " Logic error ?",
                        MessageType.error);
                return;
            }
            String recordName = attributes.get("name");
            if(recordName==null || recordName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            String typeName = attributes.get("type");
            PVRecord pvRecord = null;
            pvRecord = pvDatabase.findRecord(recordName);
            if(pvRecord!=null) {
                if(typeName!=null && typeName.length()>0) {
                    iocxmlReader.message(
                        "type " + typeName + " is ignored because record already exists",
                        MessageType.info);
                }
            } else {
                PVStructure pvStructure = null;
                if(typeName!=null) {
                    pvStructure = pvDatabase.findStructure(typeName);
                    if(pvStructure==null) {
                        iocxmlReader.message(
                                "type " + typeName + " is not a known structure",
                                MessageType.warning);
                    }
                }
                
                pvRecord = pvDataCreate.createPVRecord(recordName, pvStructure);
                if(pvStructure!=null) {
                    convert.copyStructure(pvStructure, pvRecord.getPVStructure());
                }
            }
            structureState = new StructureState();
            structureState.prevState = state;
            structureState.pvStructure = pvRecord.getPVStructure();
            state = State.record;
            
        }
        
        private void endRecord(String qName)
        {
            PVRecord pvRecord = structureState.pvStructure.getPVRecord();
            if(!pvDatabase.addRecord(pvRecord)) {
                iocxmlReader.message(
                        "record " + pvRecord.getRecordName() + " not added to database",
                        MessageType.warning);
            }
            state = structureState.prevState;
            structureState = null;
        }
        
        private void startStructure(String qName,Map<String,String> attributes)
        {
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            PVStructure pvStructure = null;
            if(structureState==null) {// is structure being defined
                if(state!=State.idle) {
                    iocxmlReader.message("Logic error startStructure state not idle",MessageType.error);
                    return;
                }
                pvStructure = pvDatabase.findStructure(fieldName);
                if(pvStructure==null) {
                    pvStructure = pvDataCreate.createPVStructure(null,fieldName, new Field[0]);
                }
            } else {// field of existing structure
                structureStack.push(structureState);
                PVStructure pvParent = structureState.pvStructure;
                PVStructure pvType = null;
                String typeName = attributes.get("type");
                if(typeName!=null && typeName.length()<=0) typeName = null;
                if(typeName!=null) {
                	pvType = pvDatabase.findStructure(typeName);
                	if(pvType==null) {
                		iocxmlReader.message(
            					"type " + typeName + " not a known structure",
            					MessageType.warning);
                	}
                }
                PVField pvField = null;
            	pvField = pvParent.getSubField(fieldName);
            	if(pvField!=null&&pvField.getField().getType()!=Type.structure) {
        			iocxmlReader.message(
        					fieldName + " field already exists and is not a structure",
        					MessageType.fatalError);
        			return;
        		}
                if(pvType==null) {
                	if(pvField!=null) {
                		pvStructure = (PVStructure)pvField;
                	} else {// add new field
                        pvStructure = pvDataCreate.createPVStructure(pvParent, fieldName,pvType);
                        pvParent.appendPVField(pvStructure);
                    }
                } else {
                	pvStructure = pvDataCreate.createPVStructure(pvParent, fieldName,pvType);
                    if(pvField==null) {
                    	pvParent.appendPVField(pvStructure);
                    } else {
                        pvField.replacePVField(pvStructure);	
                    }
                }
                
            }
            structureState = new StructureState();
            structureState.prevState = state;
            structureState.pvStructure = pvStructure;
            state = State.structure;
            
        }
        
        private void endStructure(String qName)
        {
            PVStructure pvStructure = structureState.pvStructure;
            if(structureStack.size()==0) {
                pvDatabase.addStructure(pvStructure);
                state = structureState.prevState;
                structureState = null;
                return;
            }
            state = structureState.prevState;
            structureState = structureStack.pop();
        }
        
        
        private void startScalar(String qName,Map<String,String> attributes)
        {
        	scalarBuilder.setLength(0);
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            String typeName = attributes.get("type");
            if(typeName!=null && typeName.length() <= 0) typeName = null;
            PVStructure pvStructure = structureState.pvStructure;
            PVScalar pvScalar = null;
            PVField pvField = pvStructure.getSubField(fieldName);
            if(pvField!=null) {
                if(typeName!=null) {
                    iocxmlReader.message(
                            "type is ignored because field already exists",
                            MessageType.warning);
                }
                if(pvField.getField().getType()!=Type.scalar) {
                    iocxmlReader.message(
                            "field is not a scalar",
                            MessageType.error);
                    return;
                }
                pvScalar = (PVScalar)pvField;
            } else {
                if(typeName==null) {
                    iocxmlReader.message("type not defined",MessageType.error);
                    return;
                }
                ScalarType scalarType = ScalarType.getScalarType(typeName);
                if(scalarType==null) {
                    iocxmlReader.message("type is not a valid",MessageType.error);
                    return;
                }
                pvScalar= pvDataCreate.createPVScalar(pvStructure, fieldName,scalarType);
                pvStructure.appendPVField(pvScalar);
            }
            this.pvScalar = pvScalar;
            scalarPrevState = state;
            state = State.scalar;
        }
        private void endScalar(String qName)
        {
            String value = scalarBuilder.toString();
            if(value!=null && value.length()>0) {
                convert.fromString(pvScalar, value);
            }
            scalarBuilder.setLength(0);
            pvScalar = null;
            state = scalarPrevState;
            scalarPrevState = null;
        }
        
        private void charactersScalar(char[] ch, int start, int length)
        {
            while(start<ch.length && length>0
                    && Character.isWhitespace(ch[start])) {
                start++; length--;
            }
            while(length>0 && Character.isWhitespace(ch[start+ length-1])) {
                length--;
            }
            if(length<=0) return;
            scalarBuilder.append(ch,start,length);
        }
        
        private void startScalarArray(String qName,Map<String,String> attributes)
        {
            arrayBuilder.setLength(0);
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            String typeName = attributes.get("type");
            if(typeName!=null && typeName.length() <= 0) typeName = null;
            PVStructure pvStructure = structureState.pvStructure;
            PVArray pvArray = null;
            PVField pvField = pvStructure.getSubField(fieldName);
            if(pvField!=null) {
                if(typeName!=null) {
                    iocxmlReader.message(
                            "type is ignored because field already exists",
                            MessageType.warning);
                }
                if(pvField.getField().getType()!=Type.scalarArray) {
                    iocxmlReader.message(
                            "field is not a scalarArray",
                            MessageType.error);
                    return;
                }
                pvArray = (PVArray)pvField;
            } else {
                if(typeName==null) {
                    iocxmlReader.message("type not defined",MessageType.error);
                    return;
                }
                ScalarType scalarType = ScalarType.getScalarType(typeName);
                pvArray= pvDataCreate.createPVArray(pvStructure, fieldName, scalarType);
                pvStructure.appendPVField(pvArray);
            }
            this.pvArray = pvArray;
            arrayPrevState = state;
            capacity = 0;
            length = 0;
            offset = 0;
            capacityMutable = true;
            String value = attributes.get("capacity");
            if(value!=null && value.length()>0) {
                try {
                    capacity = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    iocxmlReader.message(
                            e.toString(),
                            MessageType.warning);
                }
            }
            value = attributes.get("length");
            if(value!=null && value.length()>0) {
                try {
                    length = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    iocxmlReader.message(
                            e.toString(),
                            MessageType.warning);
                }
            }
            value = attributes.get("offset");
            if(value!=null && value.length()>0) {
                try {
                    offset = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    iocxmlReader.message(
                            e.toString(),
                            MessageType.warning);
                }
            }
            value = attributes.get("capacityMutable");
            if(value!=null && value.length()>0) {
                capacityMutable = Boolean.parseBoolean(value);
            }
            state = State.scalarArray;
        }
        
        private void endScalarArray(String qName)
        {
            String value = arrayBuilder.toString();
            if(value!=null && value.length()>0) {
                String[] values = null;
                ScalarType type = pvArray.getArray().getElementType();
                if(type!=ScalarType.pvString) {
                    values = primitivePattern.split(value);
                } else {
                    // ignore blanks , is separator
                    values = stringPattern.split(value);
                    for(int i=0; i<values.length; i++) {
                        String item = values[i];
                        int len = item.length();
                        if(len>1) {
                            if(item.charAt(0)=='\"' && item.endsWith("\"")) {
                                values[i] = item.substring(1, len-1);
                            }
                        }
                    }
                }
                if(capacity>0) pvArray.setCapacity(capacity);
                if(length>0) pvArray.setLength(length);
                try {
                    convert.fromStringArray(pvArray,offset,values.length,values,0);
                } catch (NumberFormatException e) {
                    iocxmlReader.message(
                        e.toString(),
                        MessageType.warning);
                }
            }
            if(!capacityMutable) pvArray.setCapacityMutable(false);
            arrayBuilder.setLength(0);
            pvArray = null;
            state = arrayPrevState;
            arrayPrevState = null;
        }
        
        private void charactersScalarArray(char[] ch, int start, int length)
        {
            while(start<ch.length && length>0
                    && Character.isWhitespace(ch[start])) {
                start++; length--;
            }
            while(length>0 && Character.isWhitespace(ch[start+ length-1])) {
                length--;
            }
            if(length<=0) return;
            arrayBuilder.append(ch,start,length);
        }
        
        private void startAttribute(String qName,Map<String,String> attributes)
        {
        	auxInfoBuilder.setLength(0);
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            String typeName = attributes.get("type");
            if(typeName==null || typeName.length() == 0) {
                iocxmlReader.message("type not defined",MessageType.error);
                return;
            }
            auxInfoType = ScalarType.getScalarType(typeName);
            if(auxInfoType==null) {
                iocxmlReader.message("type not a valid ScalarType",MessageType.error);
                return;
            }
            auxInfoName = fieldName;
            auxInfoPrevState = state;
            state = State.auxInfo;
        }
        
        private void endAttribute(String qName)
        {
            String value = auxInfoBuilder.toString();
            auxInfoBuilder.setLength(0);
            state = auxInfoPrevState;
            auxInfoPrevState = null;
            PVField pvField = null;
            if(state==State.scalar) {
                pvField = pvScalar;
            } else if(state==State.scalarArray) {
                pvField = pvArray;
            } else {
                pvField = structureState.pvStructure;
            }
            PVAuxInfo pvAttribute = pvField.getPVAuxInfo();
            PVScalar pvScalar = pvAttribute.createInfo(auxInfoName, auxInfoType);
            if(value!=null&&value.length()>0) {
                convert.fromString(pvScalar, value);
            }
        }
        
        private void charactersAttribute(char[] ch, int start, int length)
        {
            while(start<ch.length && length>0
                    && Character.isWhitespace(ch[start])) {
                start++; length--;
            }
            while(length>0 && Character.isWhitespace(ch[start+ length-1])) {
                length--;
            }
            if(length<=0) return;
            auxInfoBuilder.append(ch,start,length);
        }
        
    }  
}
