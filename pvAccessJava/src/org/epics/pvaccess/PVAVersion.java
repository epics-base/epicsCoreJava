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

package org.epics.pvaccess;

/**
 * pvAccessJava module version (to be keep in sync with pom.xml).
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public interface PVAVersion {

    /**
     * Major version.
     */
    public static final int VERSION_MAJOR = 4;
    
    /**
     * Minor version.
     */
    public static final int VERSION_MINOR = 0;

    /**
     * Maintenance version.
     */
    public static final int VERSION_MAINTENANCE = 2;

    /**
     * Development version.
     */
    public static final boolean VERSION_DEVELOPMENT = false;

}
