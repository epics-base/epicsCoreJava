/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import java.util.regex.Pattern;

import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVString;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.ScalarType;

/**
 * @author mrk
 *
 */
class CreateRequestImpl {
    private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
    private static final Pattern commaPattern = Pattern.compile("[,]");
    private static final Pattern equalPattern = Pattern.compile("[=]");
    
	
	static PVStructure createRequest(String request,Requester requester) {
		if(request!=null) request = request.trim();
    	if(request==null || request.length()<=0) {
    		PVStructure pvStructure =  pvDataCreate.createPVStructure(null,"", new Field[0]);
    		return pvStructure;
    	}
		int offsetRecord = request.indexOf("record[");
		int offsetField = request.indexOf("field(");
		int offsetPutField = request.indexOf("putField(");
		int offsetGetField = request.indexOf("getField(");
		PVStructure pvStructure =  pvDataCreate.createPVStructure(null,"", new Field[0]);
		if(offsetRecord>=0) {
			int offsetBegin = request.indexOf('[', offsetRecord);
			int offsetEnd = request.indexOf(']', offsetBegin);
			if(offsetEnd==-1) {
				requester.message("record[ does not have matching ]", MessageType.error);
				return null;
			}
			PVStructure pvStruct = pvDataCreate.createPVStructure(pvStructure, "record", new Field[0]);
			if(!createRequestOptions(pvStruct,request.substring(offsetBegin+1, offsetEnd),requester)) return null;
			pvStructure.appendPVField(pvStruct);
		}
		if(offsetField>=0) {
			int offsetBegin = request.indexOf('(', offsetField);
			int offsetEnd = request.indexOf(')', offsetBegin);
			if(offsetEnd==-1) {
				requester.message("field( does not have matching )", MessageType.error);
				return null;
			}
			PVStructure pvStruct = pvDataCreate.createPVStructure(pvStructure, "field", new Field[0]);
			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd),true,requester)) return null;
			pvStructure.appendPVField(pvStruct);
		}
		if(offsetPutField>=0) {
			int offsetBegin = request.indexOf('(', offsetPutField);
			int offsetEnd = request.indexOf(')', offsetBegin);
			if(offsetEnd==-1) {
				requester.message("putField( does not have matching )", MessageType.error);
				return null;
			}
			PVStructure pvStruct = pvDataCreate.createPVStructure(pvStructure, "putField", new Field[0]);
			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd),true,requester)) return null;
			pvStructure.appendPVField(pvStruct);
		}
		if(offsetGetField>=0) {
			int offsetBegin = request.indexOf('(', offsetGetField);
			int offsetEnd = request.indexOf(')', offsetBegin);
			if(offsetEnd==-1) {
				requester.message("getField( does not have matching )", MessageType.error);
				return null;
			}
			PVStructure pvStruct = pvDataCreate.createPVStructure(pvStructure, "getField", new Field[0]);
			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd),true,requester)) return null;
			pvStructure.appendPVField(pvStruct);
		}
		if(pvStructure.getStructure().getFields().length==0) {
			if(!createFieldRequest(pvStructure,request,true,requester)) return null;
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
	
    private static boolean createFieldRequest(PVStructure pvParent,String request,boolean fieldListOK,Requester requester) {
    	request = request.trim();
    	if(request.length()<=0) return true;
    	int comma = request.indexOf(',');
    	int openBrace = request.indexOf('{');
    	int openBracket = request.indexOf('[');
    	if(openBrace>=0 || openBracket>=0) fieldListOK = false;
    	if(openBrace>=0 && (comma==-1 || comma>openBrace)) {
    		//find matching brace
    		int closeBrace = findMatchingBrace(request,openBrace+1,1);
    		if(closeBrace==-1) {
    			requester.message(request + "mismatched { }", MessageType.error);
    			return false;
    		}
    		String fieldName = request.substring(0,openBrace);
    		PVStructure pvStructure = pvDataCreate.createPVStructure(pvParent, fieldName, new Field[0]);
    		createFieldRequest(pvStructure,request.substring(openBrace+1,closeBrace),false,requester);
    		pvParent.appendPVField(pvStructure);
    		if(request.length()>closeBrace+1) {
    			if(request.charAt(closeBrace+1) != ',') {
    				requester.message(request + "misssing , after }", MessageType.error);
    				return false;
    			}
    			if(!createFieldRequest(pvParent,request.substring(closeBrace+2),false,requester)) return false;;
    		}
    		return true;
    	}
    	if(openBracket==-1 && fieldListOK) {
    			PVString pvString = (PVString)pvDataCreate.createPVScalar(pvParent, "fieldList", ScalarType.pvString);
    			pvString.put(request);
    			pvParent.appendPVField(pvString);
    			return true;
    	}
    	if(openBracket!=-1 && (comma==-1 || comma>openBracket)) {
    		int closeBracket = request.indexOf(']');
			if(closeBracket==-1) {
				throw new IllegalArgumentException(request + "option does not have matching []");
			}
			if(!createLeafFieldRequest(pvParent,request.substring(0, closeBracket+1),requester)) return false;
			if(request.lastIndexOf(',')>closeBracket) {
				int nextComma = request.indexOf(',', closeBracket);
				if(!createFieldRequest(pvParent,request.substring(nextComma+1),false,requester)) return false;
			} 
			return true;
    	}
    	if(comma!=-1) {
    		if(!createLeafFieldRequest(pvParent,request.substring(0, comma),requester)) return false;
    		return createFieldRequest(pvParent,request.substring(comma+1),false,requester);
    	}
    	return createLeafFieldRequest(pvParent,request,requester);
    }
   
    public static boolean createLeafFieldRequest(PVStructure pvParent,String request,Requester requester) {
    	int openBracket = request.indexOf('[');
    	String fullName = request;
    	if(openBracket>=0) fullName = request.substring(0,openBracket);
    	int indLast = fullName.lastIndexOf('.');
		String fieldName = fullName;
		if(indLast>1) fieldName = fullName.substring(indLast+1);
    	PVStructure pvStructure = pvDataCreate.createPVStructure(pvParent, fieldName, new Field[0]);
		PVStructure pvLeaf = pvDataCreate.createPVStructure(pvStructure,"leaf", new Field[0]);
		PVString pvString = (PVString)pvDataCreate.createPVScalar(pvLeaf, "source", ScalarType.pvString);
		pvString.put(fullName);
		pvLeaf.appendPVField(pvString);
		if(openBracket>0) {
			int closeBracket = request.indexOf(']');
			if(closeBracket==-1) {
				requester.message("option does not have matching []", MessageType.error);
				return false;
			}
			if(!createRequestOptions(pvLeaf,request.substring(openBracket+1, closeBracket),requester)) return false;
		}
		pvStructure.appendPVField(pvLeaf);
		pvParent.appendPVField(pvStructure);
		return true;
    }
    
    private static boolean createRequestOptions(PVStructure pvParent,String request,Requester requester) {
		request = request.trim();
		if(request.length()<=1) return true;
    	String[] items = commaPattern.split(request);
    	for(int j=0; j<items.length; j++) {
    		String[] names = equalPattern.split(items[j]);
    		if(names.length!=2) {
    			requester.message("illegal option ",MessageType.error);
    			return false;
    		}
    		PVString pvString = (PVString)pvDataCreate.createPVScalar(pvParent, names[0], ScalarType.pvString);
    		pvString.put(names[1]);
    		pvParent.appendPVField(pvString);
        }
    	return true;
    }
}
