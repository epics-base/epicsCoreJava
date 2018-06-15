/*
 * Copyright (c) 2009 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */
package org.epics.pvaccess.util.configuration;

/**
 * Configuration interface.
 * @author msekoranja
 */
public interface Configuration {
	boolean getPropertyAsBoolean(String name, boolean defaultValue);
	int getPropertyAsInteger(String name, int defaultValue);
	float getPropertyAsFloat(String name, float defaultValue);
	String getPropertyAsString(String name, String defaultValue);
}
