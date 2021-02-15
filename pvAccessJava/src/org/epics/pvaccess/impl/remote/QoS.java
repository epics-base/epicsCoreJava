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

package org.epics.pvaccess.impl.remote;

/**
 * QoS bit-mask values.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public enum QoS {

	/**
	 * Default behavior.
	 */
	DEFAULT(0x00),

	/**
	 * Require reply (acknowledgment for reliable operation).
	 */
	REPLY_REQUIRED(0x01),

	/**
	 * Best-effort option (no reply).
	 */
	UNUSED_0(0x02),

	/**
	 * Process option.
	 */
	PROCESS(0x04),

	/**
	 * Initialize option.
	 */
	INIT(0x08),

	/**
	 * Destroy option.
	 */
	DESTROY(0x10),

	/**
	 * Share data option.
	 */
	UNUSED_1(0x20),

	/**
	 * Get.
	 */
	GET(0x40),

	/**
	 * Get-put.
	 */
	GET_PUT(0x80);



	/**
	 * Bit-mask value of this option.
	 */
	private final int maskValue;

	/**
	 * Private constructor.
	 * @param maskValue mask value
	 */
	private QoS(int maskValue)
	{
		this.maskValue = maskValue;
	}

	/**
	 * Get bit-mask value of this option.
	 * @return bit-mask value.
	 */
	public int getMaskValue()
	{
		return maskValue;
	}

	/**
	 * Check if option is set.
	 * @param qos QoS options to check.
	 * @return <code>true</code> if option is set.
	 */
	public boolean isSet(int qos)
	{
		return ((qos & getMaskValue()) != 0);
	}
}
