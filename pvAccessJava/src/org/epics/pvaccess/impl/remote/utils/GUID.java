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

package org.epics.pvaccess.impl.remote.utils;

import java.util.Arrays;

import org.epics.pvaccess.util.HexDump;

public class GUID {
	private final byte[] guid;

	public GUID(byte[] guid) {
		this.guid = guid;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(guid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GUID other = (GUID) obj;
		if (!Arrays.equals(guid, other.guid))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer(50);
		b.append("Ox");
		for (byte v : guid)
			b.append(HexDump.toHex(v));
		return b.toString();
	}
	
	public static String toString(byte[] guid) {
		StringBuffer b = new StringBuffer(50);
		b.append("Ox");
		for (byte v : guid)
			b.append(HexDump.toHex(v));
		return b.toString();
	}

}