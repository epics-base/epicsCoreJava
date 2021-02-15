/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.misc.BitSet;

/**
 * @author mse
 */
public interface CredentialsListener {
	void credentialsChanged(BitSet[] credentials);
}
