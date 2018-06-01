/*
 * Copyright (c) 2004 by Cosylab
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

package org.epics.pvaccess.util.logging;

import java.util.logging.Level;

import org.epics.pvdata.pv.MessageType;


/**
 * Implementation of Java Logging API logging utilities.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public final class LoggingUtils {

	/**
	 * Maps MessageType to standard Java Logging API Level.
	 * @param messageType message type.
	 * @return logging <code>Level</code>, <code>Level.INFO</code> if unknown.
	 */
	public static Level toLevel(MessageType messageType) {
		switch (messageType)
		{
			case info:
				return Level.INFO;
			case warning:
				return Level.WARNING;
			case error:
			case fatalError:
				return Level.SEVERE;
			default:
				return Level.INFO;
		}
	}

}
