/**
 * 
 */
package org.epics.ca.util.configuration.impl;

import org.epics.ca.util.configuration.Configuration;

/**
 * Configuration that reads config from system environment and JVM properties (higher priority).
 * @author msekoranja
 */
public class SystemConfigurationImpl implements Configuration {
	
	/* (non-Javadoc)
	 * @see org.epics.ca.util.configuration.Configuration#getPropertyAsBoolean(java.lang.String, boolean)
	 */
	@Override
	public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
		return Boolean.parseBoolean(getPropertyAsString(name, String.valueOf(defaultValue)));
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.util.configuration.Configuration#getPropertyAsInteger(java.lang.String, int)
	 */
	@Override
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
	 * @see org.epics.ca.util.configuration.Configuration#getPropertyAsFloat(java.lang.String, float)
	 */
	@Override
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
	 * @see org.epics.ca.util.configuration.Configuration#getPropertyAsString(java.lang.String, java.lang.String)
	 */
	@Override
	public String getPropertyAsString(String name, String defaultValue) {
		final String sysEnv = System.getenv(name);
		if (sysEnv != null)
			defaultValue = sysEnv;
		return System.getProperty(name, defaultValue);
	}

}
