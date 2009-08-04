/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.xml;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Array;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVAuxInfo;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDatabase;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVInt;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVScalar;
import org.epics.pvData.pv.PVStringArray;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Scalar;
import org.epics.pvData.pv.ScalarType;
import org.epics.pvData.pv.StringArrayData;
import org.epics.pvData.pv.Type;



/**
 * Factory to convert an xml file to an IOCDatabase and put it in the database.
 * The only public methods are two versions of convert.
 * @author mrk
 *
 */
public class XMLToPVDatabaseFactory {
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final AtomicBoolean isInUse = new AtomicBoolean(false);
    private static IncludeSubstituteXMLReader iocxmlReader = IncludeSubstituteXMLReaderFactory.getReader();
    //  for use by private classes
    private static final Convert convert = ConvertFactory.getConvert();
    private static final Pattern commaPattern = Pattern.compile("[,]");
    private static PVDatabase pvDatabase;
    private static IncludeSubstituteXMLListener isListener;
    private static XMLToPVDatabaseListener pvListener;
    
    /**
     * Convert an xml file to PVDatabase definitions and put the definitions in a database.
     * @param pvDatabase The database into which the new structure and records are added.
     * @param fileName The name of the file containing xml record instance definitions.
     * @param requester The requester.
     * @param reportSubstitutionFailure Should an error be reported if a ${from} does not have a substitution.
     * @param pvListener The XMLToPVDatabaseListener. This can be null.
     * @param isListener The IncludeSubstituteXMLListener listener. This can be null.
     * @param detailsListener The IncludeSubstituteDetailsXMLListener. This can be null;
     * 
     */
    public static void convert(PVDatabase pvDatabase, String fileName,Requester requester,
            boolean reportSubstitutionFailure,
            XMLToPVDatabaseListener pvListener,
            IncludeSubstituteXMLListener isListener,
            IncludeSubstituteDetailsXMLListener detailsListener)
    {
        boolean gotIt = isInUse.compareAndSet(false,true);
        if(!gotIt) {
            requester.message("XMLToIOCDBFactory.convert is already active",MessageType.fatalError);
        }
        try {
            XMLToPVDatabaseFactory.pvDatabase = pvDatabase;
            XMLToPVDatabaseFactory.isListener = isListener;
            XMLToPVDatabaseFactory.pvListener = pvListener;
            IncludeSubstituteXMLListener listener = new Listener();
            iocxmlReader.parse("database",fileName,requester,reportSubstitutionFailure,listener,detailsListener);
        } finally {
            isInUse.set(false);
        }
        
    }
    /**
     * Convert an xml file to PVDatabase definitions and put the definitions in a database.
     * Calls previous method with reportSubstitutionFailure=true and (pvListener,isListener,detailsListener) all null.
     * @param pvDatabase The database into which the new structure and records are added.
     * @param fileName The name of the file containing xml record instance definitions.
     * @param requester The requester.
     */
    public static void convert(PVDatabase pvDatabase, String fileName,Requester requester)
    {
        XMLToPVDatabaseFactory.convert(pvDatabase,fileName,requester,true,null,null,null);
    }
    
    /**
     * @author mrk
     *
     */
    private static class Listener implements IncludeSubstituteXMLListener
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
            State prevState = null;
            PVStructure pvStructure;
            boolean isEnumerated;
            boolean isChoice;
            PVInt pvIndex = null;
            PVStringArray pvChoices = null;
        }
        private Stack<StructureState> structureStack = new Stack<StructureState>();
        private StructureState structureState = null;
        
        private PVScalar pvScalar = null;
        private String scalarString = null;
        private State scalarPrevState = null;
        
        private PVArray pvArray = null;
        private String arrayString = null;
        private State arrayPrevState = null;
        private boolean immutable = false;
        private int capacity = 0;
        private int length = 0;
        private int offset = 0;
        private boolean capacityMutable = true;
        
        private String auxInfoName = null;
        private ScalarType auxInfoType = null;
        private String auxInfoString = null;
        private State auxInfoPrevState = null;
 
        private String packageName = null;
        private ArrayList<String> importNameList = new ArrayList<String>();
        
        private StringArrayData stringArrayData = new StringArrayData();
        
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#endDocument()
         */
        public void endDocument() {
            if(isListener!=null) isListener.endDocument();
        }       
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#startElement(java.lang.String, java.util.Map)
         */
        public void startElement(String name,Map<String,String> attributes)
        {
            if(isListener!=null) isListener.startElement(name, attributes);
            switch(state) {
            case idle:
                if(name.equals("structure")) {
                    startStructure(name,attributes);
                } else if(name.equals("record")) {
                    startRecord(name,attributes);
                } else if(name.equals("package")) {
                    String value = attributes.get("name");
                    if(value==null || value.length() == 0) {
                        iocxmlReader.message("name not defined",MessageType.error);
                    } else if(value.indexOf('.')<=0) {
                        iocxmlReader.message("name must have at least one embeded .",MessageType.error);
                    } else {
                        packageName = value;
                    }
                } else if(name.equals("import")) {
                    String value = attributes.get("name");
                    if(value==null || value.length() == 0) {
                        iocxmlReader.message("name not defined",MessageType.error);
                    } else if(value.indexOf('.')<=0) {
                        iocxmlReader.message("name must have at least one embeded .",MessageType.error);
                    } else {
                        importNameList.add(value);
                    }
                } else if(name.equals("auxInfo")) {
                    iocxmlReader.message(
                            "auxInfo not valid in idle state",
                            MessageType.info);
                } else {
                    iocxmlReader.message(
                            "startElement " + name + " not understood",
                            MessageType.info);
                }
                return;
            case record:
            case structure:
                if(name.equals("structure")) {
                    startStructure(name,attributes);
                } else if(name.equals("scalar")) {
                    startScalar(name,attributes);
                } else if(name.equals("array")) {
                    startScalarArray(name,attributes);
                } else if(name.equals("auxInfo")) {
                    startAuxInfo(name,attributes);
                } else {
                    iocxmlReader.message(
                            "startElement " + name + " not understood current state is structure",
                            MessageType.error);
                }
                return;
            case scalar:
                if(name.equals("auxInfo")) {
                    startAuxInfo(name,attributes);
                } else {
                    iocxmlReader.message(
                            "startElement " + name + " not understood current state is scalar",
                            MessageType.error);
                }
                return;
            case scalarArray:
                if(name.equals("auxInfo")) {
                    startAuxInfo(name,attributes);
                } else {
                    iocxmlReader.message(
                            "startElement " + name + " not understood current state is scalarArray",
                            MessageType.error);
                }
                return;
            case auxInfo:
                iocxmlReader.message(
                        "startElement " + name + " not understood current state is auxInfo",
                        MessageType.error);
                return;
            default:
                iocxmlReader.message(
                        "startElement " + name + " Logic Error in parser",
                        MessageType.error);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#endElement(java.lang.String)
         */
        public void endElement(String name)
        {
            if(isListener!=null) isListener.endElement(name);
            switch(state) {
            case idle:
                return;
            case record:
                if(name.equals("record")) {
                    endRecord(name);
                } else {
                    iocxmlReader.message(
                            "endElement " + name + " not understood",
                            MessageType.error);
                }
                return;
            case structure:
                if(name.equals("structure")) {
                    endStructure(name);
                } else {
                    iocxmlReader.message(
                            "endElement " + name + " not understood",
                            MessageType.error);
                }
                return;
            case scalar:
                if(name.equals("scalar")) {
                    endScalar(name);
                } else {
                    iocxmlReader.message(
                            "endElement " + name + " not understood",
                            MessageType.error);
                }
                return;
            case scalarArray:
                if(name.equals("array")) {
                    endScalarArray(name);
                } else {
                    iocxmlReader.message(
                            "endElement " + name + " not understood",
                            MessageType.error);
                }
                return;
            case auxInfo:
                if(name.equals("auxInfo")) {
                    endAuxInfo(name);
                } else {
                    iocxmlReader.message(
                        "endElement " + name + " not understood",
                        MessageType.error);
                }
                return;
            default:
                iocxmlReader.message(
                        "endElement " + name + " Logic Error in parser",
                        MessageType.error);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.xml.IncludeSubstituteXMLListener#element(java.lang.String)
         */
        public void element(String content) {
            if(isListener!=null) isListener.element(content);
            switch(state) {
            case idle:
                return;
            case structure:
                return;
            case scalar:
                scalarString = content;
                return;
            case scalarArray:
                arrayString = content;
            case auxInfo:
                auxInfoString = content;
                return;
            default:
                return;
            }
        }
        
        private String findExtendedStructureName(String name) {
            if(name.indexOf('.')>=0) return name;
            String[] databaseNames = pvDatabase.getStructureNames();
            String[] masterNames = pvDatabase.getMaster().getStructureNames();
            for(String importName: importNameList) {
                boolean wildCard = false;
                if(importName.endsWith("*")) {
                    wildCard = true;
                    importName = importName.substring(0, importName.lastIndexOf("*"));
                }
                String[] names = databaseNames;
                while(names!=null) {
                    for(String structName: names) {
                        if(structName.indexOf('.')<0) continue;
                        if(!wildCard) {
                            String trialName = structName.substring(structName.lastIndexOf('.'));
                            if(trialName.equals(name)) {
                                return structName;
                            }
                        } else {
                            if(structName.equals(importName + name)) {
                                return structName;
                            }
                        }
                    }
                    if(names==databaseNames) {
                        names = masterNames;
                    } else {
                        names = null;
                    }
                }
            }
            return name;
        }
        
        private void startRecord(String name,Map<String,String> attributes)
        {
            if(state!=State.idle) {
                iocxmlReader.message(
                        "startElement " + name + " not allowed except as top level structure",
                        MessageType.error);
                return;
            }
            if(structureStack.size()!=0) {
                iocxmlReader.message(
                        "startElement " + name + " Logic error ?",
                        MessageType.error);
                return;
            }
            String recordName = attributes.get("recordName");
            if(recordName==null || recordName.length() == 0) {
                iocxmlReader.message("recordName not defined",MessageType.error);
                return;
            }
            String extendsName = attributes.get("extends");
            PVRecord pvRecord = null;
            pvRecord = pvDatabase.findRecord(recordName);
            if(pvRecord!=null) {
                if(extendsName!=null && extendsName.length()>0) {
                    iocxmlReader.message(
                        "type " + extendsName + " is ignored because record already exists",
                        MessageType.info);
                }
            } else {
                PVStructure pvStructure = null;
                if(extendsName!=null) {
                    extendsName = findExtendedStructureName(extendsName);
                    pvStructure = pvDatabase.findStructure(extendsName);
                    if(pvStructure==null) {
                        iocxmlReader.message(
                                "type " + extendsName + " is not a known structure",
                                MessageType.error);
                    }
                }
                
                pvRecord = pvDataCreate.createPVRecord(recordName, pvStructure);
                if(pvStructure!=null) {
                    pvRecord.putExtendsStructureName(extendsName);
                }
            }
            structureState = new StructureState();
            structureState.prevState = state;
            structureState.pvStructure = pvRecord.getPVStructure();
            structureState.isEnumerated = false;
            structureState.isChoice = false;
            state = State.record;
            if(pvListener!=null) pvListener.startRecord(pvRecord);
            
        }
        
        private void endRecord(String name)
        {
            if(pvListener!=null) pvListener.endRecord();   
            PVRecord pvRecord = structureState.pvStructure.getPVRecord();
            if(!pvDatabase.addRecord(pvRecord)) {
                iocxmlReader.message(
                        "record " + pvRecord.getRecordName() + " not added to database",
                        MessageType.error);
            }
            state = structureState.prevState;
            structureState = null;
        }
        
        private void startStructure(String name,Map<String,String> attributes)
        {
            
            PVStructure pvStructure = null;
            if(structureState==null) {// is structure being defined
                String structureName = attributes.get("structureName");
                if(structureName==null || structureName.length() == 0) {
                    iocxmlReader.message("name not defined",MessageType.error);
                    return;
                }
                if(state!=State.idle) {
                    iocxmlReader.message("Logic error startStructure state not idle",MessageType.error);
                    return;
                }
                if(packageName!=null) structureName = packageName + "." + structureName;
                pvStructure = pvDatabase.findStructure(structureName);
                if(pvStructure==null) {
                    PVStructure pvType = null;
                    String extendsName = attributes.get("extends");
                    if(extendsName!=null && extendsName.length()<=0) extendsName = null;
                    if(extendsName!=null) {
                        extendsName = findExtendedStructureName(extendsName);
                        pvType = pvDatabase.findStructure(extendsName);
                        if(pvType==null) {
                            iocxmlReader.message(
                                    "type " + extendsName + " not a known structure",
                                    MessageType.error);
                        }
                    }
                    if(pvType!=null) {
                        pvStructure = pvDataCreate.createPVStructure(null, structureName, pvType);
                        pvStructure.putExtendsStructureName(extendsName);
                    } else {
                        pvStructure = pvDataCreate.createPVStructure(null,structureName, new Field[0]);
                    }
                }
                if(pvListener!=null) pvListener.startStructure(pvStructure);
            } else {// field of existing structure
                String fieldName = attributes.get("name");
                if(fieldName==null || fieldName.length() == 0) {
                    iocxmlReader.message("name not defined",MessageType.error);
                    return;
                }
                structureStack.push(structureState);
                PVStructure pvParent = structureState.pvStructure;
                PVStructure pvType = null;
                String extendsName = attributes.get("extends");
                if(extendsName!=null && extendsName.length()<=0) extendsName = null;
                if(extendsName!=null) {
                    extendsName = findExtendedStructureName(extendsName);
                	pvType = pvDatabase.findStructure(extendsName);
                	if(pvType==null) {
                		iocxmlReader.message(
            					"type " + extendsName + " not a known structure",
            					MessageType.error);
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
                	pvStructure.putExtendsStructureName(extendsName);
                    if(pvField==null) {
                    	pvParent.appendPVField(pvStructure);
                    } else {
                        pvField.replacePVField(pvStructure);	
                    }
                }
                if(pvListener!=null) pvListener.newStructureField(pvStructure);
            }
            structureState = new StructureState();
            structureState.prevState = state;
            structureState.pvStructure = pvStructure;
            PVField[] pvFields = pvStructure.getPVFields();
            // is the structure an enumerated structure?
            structureState.isEnumerated = false;
            if(pvFields.length==2) {
                PVField pvField = pvFields[0];
                Field field = pvField.getField();
                if(field.getFieldName().equals("index") && field.getType()==Type.scalar) {
                    Scalar scalar = (Scalar)field;
                    if(scalar.getScalarType()==ScalarType.pvInt) {
                        structureState.pvIndex = (PVInt)pvField;
                        pvField = pvFields[1];
                        field = pvField.getField();
                        if(field.getFieldName().equals("choices") && field.getType()==Type.scalarArray) {
                            Array array = (Array)field;
                            if(array.getElementType()==ScalarType.pvString) {
                                structureState.isEnumerated = true;
                                structureState.pvChoices = (PVStringArray)pvField;
                            }
                        }
                    }
                }
            }
            state = State.structure;
        }
        
        private void endStructure(String name)
        {
            PVStructure pvStructure = structureState.pvStructure;
            if(structureStack.size()==0) {
                pvDatabase.addStructure(pvStructure);
                state = structureState.prevState;
                structureState = null;
                if(pvListener!=null) pvListener.endStructureField();
                return;
            }
            if(pvListener!=null) pvListener.endStructure();
            state = structureState.prevState;
            structureState = structureStack.pop();
        }
        
        
        private void startScalar(String name,Map<String,String> attributes)
        {
            String immutableString = attributes.get("immutable");
            if(immutableString!=null && immutableString.equals("true")) {
                immutable = true;
            } else {
                immutable = false;
            }
            scalarString = null;
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            if(structureState.isEnumerated && fieldName.equals("choice")) {
                structureState.isChoice = true;
            } else {
                structureState.isChoice = false;
                String typeName = attributes.get("scalarType");
                if(typeName!=null && typeName.length() <= 0) typeName = null;
                PVStructure pvStructure = structureState.pvStructure;
                PVScalar pvScalar = null;
                PVField pvField = pvStructure.getSubField(fieldName);
                if(pvField!=null) {
                    if(typeName!=null) {
                        iocxmlReader.message(
                                "type is ignored because field already exists",
                                MessageType.error);
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
                        iocxmlReader.message("scalarType not defined",MessageType.error);
                        return;
                    }
                    ScalarType scalarType = ScalarType.getScalarType(typeName);
                    if(scalarType==null) {
                        iocxmlReader.message("scalarType is not a valid",MessageType.error);
                        return;
                    }
                    pvScalar= pvDataCreate.createPVScalar(pvStructure, fieldName,scalarType);
                    pvStructure.appendPVField(pvScalar);
                }
                this.pvScalar = pvScalar;
                if(pvListener!=null) pvListener.startScalar(pvScalar);
            }
            scalarPrevState = state;
            state = State.scalar;
        }
        private void endScalar(String name)
        {
            if(scalarString!=null && scalarString.length()>0) {
                if(structureState.isChoice) {
                    PVStringArray pvStringArray = structureState.pvChoices;
                    pvStringArray.get(0, pvStringArray.getLength(), stringArrayData);
                    PVInt pvInt = structureState.pvIndex;
                    String[] choices = stringArrayData.data;
                    boolean foundChoice = false;
                    for(int i=0; i<choices.length; i++) {
                        if(choices[i].equals(scalarString)) {
                            pvInt.put(i);
                            foundChoice = true;
                            break;
                        }
                    }
                    if(!foundChoice) {
                        iocxmlReader.message(scalarString + " is not a valid choice",MessageType.error);
                    }
                } else {
                    convert.fromString(pvScalar, scalarString);
                }
            }
            if(immutable) pvScalar.setImmutable();
            scalarString = null;
            pvScalar = null;
            state = scalarPrevState;
            if(pvListener!=null) pvListener.endScalar();
            scalarPrevState = null;
        }
        
        
        private void startScalarArray(String name,Map<String,String> attributes)
        {
            String immutableString = attributes.get("immutable");
            if(immutableString!=null && immutableString.equals("true")) {
                immutable = true;
            } else {
                immutable = false;
            }
            arrayString = null;
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            String typeName = attributes.get("scalarType");
            if(typeName!=null && typeName.length() <= 0) typeName = null;
            PVStructure pvStructure = structureState.pvStructure;
            PVArray pvArray = null;
            PVField pvField = pvStructure.getSubField(fieldName);
            if(pvField!=null) {
                if(typeName!=null) {
                    iocxmlReader.message(
                            "type is ignored because field already exists",
                            MessageType.error);
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
                    iocxmlReader.message("scalarType not defined",MessageType.error);
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
                            MessageType.error);
                }
            }
            value = attributes.get("length");
            if(value!=null && value.length()>0) {
                try {
                    length = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    iocxmlReader.message(
                            e.toString(),
                            MessageType.error);
                }
            }
            value = attributes.get("offset");
            if(value!=null && value.length()>0) {
                try {
                    offset = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    iocxmlReader.message(
                            e.toString(),
                            MessageType.error);
                }
            }
            value = attributes.get("capacityMutable");
            if(value!=null && value.length()>0) {
                capacityMutable = Boolean.parseBoolean(value);
            }
            state = State.scalarArray;
            if(pvListener!=null) pvListener.startArray(pvArray);
        }
        
        private void endScalarArray(String name)
        {
            String value = arrayString;
            arrayString = null;
            if(value!=null && value.length()>0) {
                if((value.charAt(0)=='[') && value.endsWith("]")) {
                    int offset = value.lastIndexOf(']');
                    value = value.substring(1, offset);
                }
            }
            if(value!=null && value.length()>0) {
                String[] values = null;
                values = commaPattern.split(value);
                ScalarType type = pvArray.getArray().getElementType();
                if(type==ScalarType.pvString) {
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
                        MessageType.error);
                }
            }
            if(immutable) pvArray.setImmutable();
            if(!capacityMutable) pvArray.setCapacityMutable(false);
            if(pvListener!=null) pvListener.endArray();
            pvArray = null;
            state = arrayPrevState;
            arrayPrevState = null;
        }
        
        
        
        private void startAuxInfo(String name,Map<String,String> attributes)
        {
        	auxInfoString = null;
            String fieldName = attributes.get("name");
            if(fieldName==null || fieldName.length() == 0) {
                iocxmlReader.message("name not defined",MessageType.error);
                return;
            }
            String typeName = attributes.get("scalarType");
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
        
        private void endAuxInfo(String name)
        {
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
            if(auxInfoString!=null && auxInfoString.length()>0) {
                convert.fromString(pvScalar, auxInfoString);
            }
            auxInfoString = null;
        }
    }  
}
