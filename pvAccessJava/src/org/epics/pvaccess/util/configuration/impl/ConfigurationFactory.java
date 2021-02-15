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
package org.epics.pvaccess.util.configuration.impl;

import java.util.HashMap;

import org.epics.pvaccess.util.configuration.Configuration;
import org.epics.pvaccess.util.configuration.ConfigurationProvider;

/**
 * Configuration factory.
 * @author msekoranja
 */
public class ConfigurationFactory {

	private static ConfigurationProvider provider;

	public static synchronized ConfigurationProvider getProvider()
	{
		if (provider == null) {
			provider = new ConfigurationProviderImpl();
			// default
			provider.registerConfiguration("system", new SystemConfigurationImpl());
		}
		return provider;
	}

	private static class ConfigurationProviderImpl implements ConfigurationProvider {

		private HashMap<String, Configuration> configs = new HashMap<String, Configuration>();

		public void registerConfiguration(String name, Configuration configuration) {
			synchronized (configs) {
				if (configs.containsKey(name))
					throw new IllegalStateException("configuration with name " + name + " already registered");
				configs.put(name, configuration);
			}
		}

		public Configuration getConfiguration(String name) {
			synchronized (configs) {
				return configs.get(name);
			}
		}

	}
}
