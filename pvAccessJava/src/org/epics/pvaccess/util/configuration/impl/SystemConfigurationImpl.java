/*
 *
 */
package org.epics.pvaccess.util.configuration.impl;

import org.epics.pvaccess.util.configuration.Configuration;

/**
 * Configuration that reads config from system environment and JVM properties (higher priority).
 * @author msekoranja
 */
public class SystemConfigurationImpl implements Configuration {

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.util.configuration.Configuration#getPropertyAsBoolean(java.lang.String, boolean)
	 */
	public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
		String value = getPropertyAsString(name, String.valueOf(defaultValue));
		value = value.toLowerCase();

		boolean isTrue = value.equals("1") || value.equals("true") || value.equals("yes");
	    if (isTrue)
	        return true;

		boolean isFalse = value.equals("0") || value.equals("false") || value.equals("no");
	    if (isFalse)
	        return false;

		// invalid value
	    return defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.util.configuration.Configuration#getPropertyAsInteger(java.lang.String, int)
	 */
	public int getPropertyAsInteger(String name, int defaultValue) {
		final String val = getPropertyAsString(name, null);
		if (val == null)
			return defaultValue;

		try {
			return Integer.parseInt(val);
		} catch (Throwable th) {
			return defaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.util.configuration.Configuration#getPropertyAsFloat(java.lang.String, float)
	 */
	public float getPropertyAsFloat(String name, float defaultValue) {
		final String val = getPropertyAsString(name, null);
		if (val == null)
			return defaultValue;

		try {
			return Float.parseFloat(val);
		} catch (Throwable th) {
			return defaultValue;
		}
	}

	/* (non-Javadoc)
	 * @see org.epics.pvaccess.util.configuration.Configuration#getPropertyAsString(java.lang.String, java.lang.String)
	 */
	public String getPropertyAsString(String name, String defaultValue) {
		final String sysEnv = System.getenv(name);
		if (sysEnv != null)
			defaultValue = sysEnv;
		return System.getProperty(name, defaultValue);
	}

}
