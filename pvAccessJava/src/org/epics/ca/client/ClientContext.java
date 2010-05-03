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

package org.epics.ca.client;

import java.io.PrintStream;

import org.epics.ca.CAException;
import org.epics.ca.Version;

/**
 * The class representing a CA Client Context.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface ClientContext {
  
  /**
   * Get context implementation version.
   * @return version of the context implementation.
   */
  public Version getVersion();

  /**
   * Initialize client context. This method is called immediately after instance construction (call of constructor).
   */
  public void initialize() throws CAException, IllegalStateException;

  /**
   * Get channel provider implementation.
   * @return the channel provider.
   */
  public ChannelProvider getProvider();

  /**
   * Prints detailed information about the context to the standard output stream.
   */
  public void printInfo();

  /**
   * Prints detailed information about the context to the specified output stream.
   * @param out the output stream.
   */
  public void printInfo(PrintStream out);

  /**
   * Clear all resources attached to this Context
   * @throws IllegalStateException if the context has been destroyed.
   */
  public void destroy() throws CAException, IllegalStateException;

  /**
   * Dispose (destroy) server context.
   * This calls <code>destroy()</code> and silently handles all exceptions.
   */
  public void dispose();
}