package org.epics.pvaccess.impl.remote.utils.getopt;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class GetoptResourceBundle extends ResourceBundle {

	public static final GetoptResourceBundle INSTANCE = new GetoptResourceBundle();
	
	private final Map<String, String> objectMap = new HashMap<String, String>();
	
	private GetoptResourceBundle()
	{
		super();
		
		objectMap.put("getopt.ambigious","{0}: option ''{1}'' is ambiguous");
		objectMap.put("getopt.arguments1","{0}: option ''--{1}'' doesn't allow an argument");
		objectMap.put("getopt.arguments2","{0}: option ''{1}{2}'' doesn't allow an argument");
		objectMap.put("getopt.requires","{0}: option ''{1}'' requires an argument");
		objectMap.put("getopt.unrecognized","{0}: unrecognized option ''--{1}''");
		objectMap.put("getopt.unrecognized2","{0}: unrecognized option ''{1}{2}''");
		objectMap.put("getopt.illegal","{0}: illegal option -- {1}");
		objectMap.put("getopt.invalid","{0}: invalid option -- {1}");
		objectMap.put("getopt.requires2","{0}: option requires an argument -- {1}");
		objectMap.put("getopt.invalidValue","Invalid value {0} for parameter 'has_arg'");		
		
	}
	
	@Override
	protected Object handleGetObject(String key) {
		return handleGetObject(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(objectMap.keySet());
	}

}
