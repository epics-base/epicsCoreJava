/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.copy;

import java.util.regex.Pattern;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.FieldCreate;
import org.epics.pvdata.pv.PVDataCreate;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

/**
 * @author mrk
 *
 */
public class CreateRequest {
	/**
	 * Create s CreateRequest.
	 * @return new instance of CreateRequest
	 */
	public static CreateRequest create()
	{
		return new CreateRequest();
	}
	/**
	 * Create a request structure for the create calls in Channel.
	 * See the package overview documentation for details.
	 * @param request The field request. See the package overview documentation for details.
	 * @return The request structure if an invalid request was given. 
	 */
	public PVStructure createRequest(String request) {
		return createRequestInternal(request);
	}
	public String getMessage() {
		return message;
	}

	private static final PVDataCreate pvDataCreate = PVDataFactory.getPVDataCreate();
	private static final FieldCreate fieldCreate = FieldFactory.getFieldCreate();
	private static final Structure emptyStructure = fieldCreate.createStructure(new String[0], new Field[0]);
	private static final Pattern commaPattern = Pattern.compile("[,]");
	private static final Pattern equalPattern = Pattern.compile("[=]");
	private String message;
	//private static final Pattern periodPattern = Pattern.compile("[.]");


	private PVStructure createRequestInternal(String request) {
		if(request!=null) request = request.replaceAll("\\s+","");
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
				message = request.substring(offsetRecord) + "record[ does not have matching ]";
				return null;
			}
			PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
			if(!createRequestOptions(pvStruct,request.substring(offsetBegin+1, offsetEnd))) return null;
			pvStructure.appendPVField("record", pvStruct);
		}
		if(offsetField>=0) {
			int offsetBegin = request.indexOf('(', offsetField);
			int offsetEnd = request.indexOf(')', offsetBegin);
			if(offsetEnd==-1) {
				message = request.substring(offsetField) + "field( does not have matching )";
				return null;
			}
			PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd))) return null;
			pvStructure.appendPVField("field", pvStruct);
		}
		if(offsetPutField>=0) {
			int offsetBegin = request.indexOf('(', offsetPutField);
			int offsetEnd = request.indexOf(')', offsetBegin);
			if(offsetEnd==-1) {
				message = request.substring(offsetPutField) + "putField( does not have matching )";
				return null;
			}
			PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd))) return null;
			pvStructure.appendPVField("putField", pvStruct);
		}
		if(offsetGetField>=0) {
			int offsetBegin = request.indexOf('(', offsetGetField);
			int offsetEnd = request.indexOf(')', offsetBegin);
			if(offsetEnd==-1) {
				message = request.substring(offsetGetField) + "getField( does not have matching )";
				return null;
			}
			PVStructure pvStruct =  pvDataCreate.createPVStructure(emptyStructure);
			if(!createFieldRequest(pvStruct,request.substring(offsetBegin+1, offsetEnd))) return null;
			pvStructure.appendPVField("getField", pvStruct);
		}
		if(pvStructure.getStructure().getFields().length==0) {
			if(!createFieldRequest(pvStructure,request)) return null;
		}
		return pvStructure;
	}

	private int findMatchingBrace(String request,int index,int numOpen) {
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

	private boolean createFieldRequest(PVStructure pvParent,String request) {
		request = request.trim();
		if(request.length()<=0) return true;
		int comma = request.indexOf(',');
		if(comma==0) {
			return createFieldRequest(pvParent,request.substring(1));
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
				return createFieldRequest(pvStructure,request);
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
				createFieldRequest(xxx,rest);
				pvParent.appendPVField(fieldName, xxx);
			} else {
				pvParent.appendPVField(nextFieldName, pvStructure);
			}
			request = request.substring(end+1);
			return createFieldRequest(pvParent,request);
		}
		if(end==openBracket) {
			int closeBracket =  request.indexOf(']');
			if(closeBracket<=0) {
				message = request + " does not have matching ]";
				return false;
			}
			String options = request.substring(openBracket+1, closeBracket);
			int period = nextFieldName.indexOf('.');
			if(period>0) {
				String fieldName = nextFieldName.substring(0,period);
				PVStructure xxx =  pvDataCreate.createPVStructure(emptyStructure);
				if(!createRequestOptions(xxx,options)) return false;
				String rest = nextFieldName.substring(period+1);
				createFieldRequest(xxx,rest);
				pvParent.appendPVField(fieldName, xxx);
			} else {
				if(!createRequestOptions(pvStructure,options)) return false;
				pvParent.appendPVField(nextFieldName, pvStructure);
			}
			request = request.substring(closeBracket+1);
			return createFieldRequest(pvParent,request);
		}
		// end== openBrace
		int closeBrace = findMatchingBrace(request,openBrace+1,1);
		if(closeBrace<=0) {
			message = request + " does not have matching }";
			return false;
		}
		String subFields = request.substring(openBrace+1, closeBrace);
		if(!createFieldRequest(pvStructure,subFields)) return false;
		request = request.substring(closeBrace+1);
		int period = nextFieldName.indexOf('.');
		if(period<=0) {
			pvParent.appendPVField(nextFieldName, pvStructure);
			return createFieldRequest(pvParent,request);
		}
		PVStructure yyy = pvParent;
		while(period>=0) {
			String fieldName = nextFieldName.substring(0,period);
			PVStructure xxx =  pvDataCreate.createPVStructure(emptyStructure);
			yyy.appendPVField(fieldName,xxx);
			nextFieldName = nextFieldName.substring(period+1);
			period = nextFieldName.indexOf('.');
			if(period<=0) {
				xxx.appendPVField(nextFieldName, pvStructure);
				break;
			}
			yyy = xxx;
		}
		return createFieldRequest(pvParent,request);
	}


	private boolean createRequestOptions(PVStructure pvParent,String request) {
		request = request.trim();
		if(request.length()<=1) return true;
		String[] items = commaPattern.split(request);
		int nitems = items.length;
		PVField[] pvFields = new PVField[nitems];
		String[] fieldNames = new String[nitems];
		for(int j=0; j<nitems; j++) {
			String[] names = equalPattern.split(items[j]);
			if(names.length!=2) {
				message = "illegal option ";
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
