/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import java.util.regex.Pattern;

import org.epics.pvaccess.PVFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
 * @author mrk
 *
 */
public class CreateRequestFactory {
	/**
     * Create a request structure for the create calls in Channel.
     * See the package overview documentation for details.
     * @param request The field request. See the package overview documentation for details.
     * @param requester The requester;
     * @return The request structure if an invalid request was given. 
     */
    public static PVStructure createRequest(String request,Requester requester) {
    	return CreateRequestImpl.createRequest(request,requester);
    }
    
    static private class CreateRequestImpl {
        private static final PVDataCreate pvDataCreate = PVFactory.getPVDataCreate();
        private static final FieldCreate fieldCreate = PVFactory.getFieldCreate();
        private static final Structure emptyStructure = fieldCreate.createStructure(new String[0], new Field[0]);
        private static final Pattern commaPattern = Pattern.compile("[,]");
        private static final Pattern equalPattern = Pattern.compile("[=]");
        
    	
    	static PVStructure createRequest(String request,Requester requester) {
    		if(request!=null) request = request.trim();
        	if(request==null || request.length()<=0) {
        		PVStructure pvStructure =  pvDataCreate.createPVStructure(emptyStructure);
        		return pvStructure;
        	}
    		int offsetRecord = request.indexOf("record[");
    		int offsetField = request.indexOf("field(");
    		int offsetPutField = request.indexOf("putField(");
    		int offsetGetField = request.indexOf("getField(");
    		PVStructure pvStructure =  pvDataCreate.createPVStructure(emptyStructure);
    		if(offsetRecord>=0) {
    			int offsetBegin = request.indexOf('[', offsetRecord);
    			int offsetEnd = request.indexOf(']', offsetBegin);
    			if(offsetEnd==-1) {
    				requester.message(request.substring(offsetRecord) + "record[ does not have matching ]", MessageType.error);
    				return null;
    			}
        		PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
    			if(!createRequestOptions(pvStruct,request.substring(offsetBegin+1, offsetEnd),requester)) return null;
    			pvStructure.appendPVField("record", pvStruct);
    		}
    		if(offsetField>=0) {
    			int offsetBegin = request.indexOf('(', offsetField);
    			int offsetEnd = request.indexOf(')', offsetBegin);
    			if(offsetEnd==-1) {
    				requester.message(request.substring(offsetField) + "field( does not have matching )", MessageType.error);
    				return null;
    			}
        		PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
    			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd),requester)) return null;
    			pvStructure.appendPVField("field", pvStruct);
    		}
    		if(offsetPutField>=0) {
    			int offsetBegin = request.indexOf('(', offsetPutField);
    			int offsetEnd = request.indexOf(')', offsetBegin);
    			if(offsetEnd==-1) {
    				requester.message(request.substring(offsetPutField) + "putField( does not have matching )", MessageType.error);
    				return null;
    			}
        		PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
    			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd),requester)) return null;
    			pvStructure.appendPVField("putField", pvStruct);
    		}
    		if(offsetGetField>=0) {
    			int offsetBegin = request.indexOf('(', offsetGetField);
    			int offsetEnd = request.indexOf(')', offsetBegin);
    			if(offsetEnd==-1) {
    				requester.message(request.substring(offsetGetField) + "getField( does not have matching )", MessageType.error);
    				return null;
    			}
        		PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
    			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd),requester)) return null;
    			pvStructure.appendPVField("getField", pvStruct);
    		}
    		if(pvStructure.getStructure().getFields().length==0) {
    			if(!createFieldRequest(pvStructure,request,requester)) return null;
    		}
        	return pvStructure;
    	}
    	
    	private static int findMatchingBrace(String request,int index,int numOpen) {
    		int openBrace = request.indexOf('{', index+1);
    		int closeBrace = request.indexOf('}', index+1);
    		if(openBrace==-1 && closeBrace==-1) return -1;
    		if(openBrace>0) {
    			if(openBrace<closeBrace) return findMatchingBrace(request,openBrace,numOpen+1);
    			if(numOpen==1) return closeBrace;
    			return findMatchingBrace(request,closeBrace,numOpen-1);
    		}
    		if(numOpen==1) return closeBrace;
    		return findMatchingBrace(request,closeBrace,numOpen-1);
    	}
    	
        private static boolean createFieldRequest(PVStructure pvParent,String request,Requester requester) {
        	request = request.trim();
        	if(request.length()<=0) return true;
        	int comma = request.indexOf(',');
        	if(comma==0) {
        	    return createFieldRequest(pvParent,request.substring(1),requester);
        	}
        	int openBrace = request.indexOf('{');
        	int openBracket = request.indexOf('[');
        	PVStructure pvStructure =  pvDataCreate.createPVStructure(emptyStructure);
        	if(comma==-1 && openBrace==-1 && openBracket==-1) {
        	    int period = request.indexOf('.');
                if(period>0) {
                    String fieldName = request.substring(0,period);
                    request = request.substring(period+1);
                    pvParent.appendPVField(fieldName, pvStructure);
                    return createFieldRequest(pvStructure,request,requester);
                }
        	    pvParent.appendPVField(request,pvStructure);
        	    return true;
        	}
        	int end = comma;
        	if(openBrace!=-1 && (end> openBrace || end==-1)) end = openBrace;
        	if(openBracket!=-1 && (end> openBracket || end==-1)) end = openBracket;
        	String nextFieldName = request.substring(0,end);
        	if(end==comma) {
        	    int period = nextFieldName.indexOf('.');
                if(period>0) {
                    String fieldName = nextFieldName.substring(0,period);
                    PVStructure xxx =  pvDataCreate.createPVStructure(emptyStructure);
                    String rest = nextFieldName.substring(period+1);
                    createFieldRequest(xxx,rest,requester);
                    pvParent.appendPVField(fieldName, xxx);
                } else {
        	        pvParent.appendPVField(nextFieldName, pvStructure);
                }
        	    request = request.substring(end+1);
        	    return createFieldRequest(pvParent,request,requester);
        	}
        	if(end==openBracket) {
        	    int closeBracket =  request.indexOf(']');
        	    if(closeBracket<=0) {
                    requester.message(request + " does not have matching ]", MessageType.error);
                    return false;
                }
        	    String options = request.substring(openBracket+1, closeBracket);
        	    if(!createRequestOptions(pvStructure,options,requester)) return false;
        	    int period = nextFieldName.indexOf('.');
                if(period>0) {
                    String fieldName = nextFieldName.substring(0,period);
                    PVStructure xxx =  pvDataCreate.createPVStructure(emptyStructure);
                    String rest = nextFieldName.substring(period+1);
                    createFieldRequest(xxx,rest,requester);
                    pvParent.appendPVField(fieldName, xxx);
                } else {
                    pvParent.appendPVField(nextFieldName, pvStructure);
                }
        	    request = request.substring(closeBracket+1);
        	    return createFieldRequest(pvParent,request,requester);
        	}
        	// end== openBrace
        	int closeBrace = findMatchingBrace(request,openBrace+1,1);
        	if(closeBrace<=0) {
        	    requester.message(request + " does not have matching }", MessageType.error);
                return false;
        	}
        	String subFields = request.substring(openBrace+1, closeBrace);
        	if(!createFieldRequest(pvStructure,subFields,requester)) return false;
        	int period = nextFieldName.indexOf('.');
            if(period>0) {
                String fieldName = nextFieldName.substring(0,period);
                PVStructure xxx =  pvDataCreate.createPVStructure(emptyStructure);
                String rest = nextFieldName.substring(period+1);
                createFieldRequest(xxx,rest,requester);
                pvParent.appendPVField(fieldName, xxx);
            } else {
                pvParent.appendPVField(nextFieldName, pvStructure);
            }
            request = request.substring(closeBrace+1);
            return createFieldRequest(pvParent,request,requester);
        }
        	
        
        private static boolean createRequestOptions(PVStructure pvParent,String request,Requester requester) {
    		request = request.trim();
    		if(request.length()<=1) return true;
        	String[] items = commaPattern.split(request);
        	int nitems = items.length;
        	PVField[] pvFields = new PVField[nitems];
        	String[] fieldNames = new String[nitems];
        	for(int j=0; j<nitems; j++) {
        		String[] names = equalPattern.split(items[j]);
        		if(names.length!=2) {
        			requester.message("illegal option ",MessageType.error);
        			return false;
        		}
        		PVString pvString = (PVString)pvDataCreate.createPVScalar(ScalarType.pvString);
        		pvString.put(names[1]);
        		fieldNames[j] = names[0];
        		pvFields[j] = pvString;
            }
        	PVStructure options = pvDataCreate.createPVStructure(fieldNames,pvFields);
        	pvParent.appendPVField("_options", options);
        	return true;
        }
    }
}
