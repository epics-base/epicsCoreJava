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
 * Administrative class to keep track of the version number.
 * @author msekoranja
 * @version $Id$
 */
public class Version {

    /**
     * @see Version#getProductName()
     */
    private final String productName;

    /**
     * @see Version#getImplementationLanguage()
     */
    private final String implementationLanguage;

    /**
     * @see Version#getMajorVersion()
     */
    private final int majorVersion;

    /**
     * @see Version#getMinorVersion()
     */
    private final int minorVersion;

    /**
     * @see Version#getMaintenanceVersion()
     */
    private final int maintenanceVersion;

    /**
     * @code Version#getDevelopmentVersion()
     */
    private final boolean developmentFlag;

    /**
     * Default constructor.
     * @param productName	product name.
     * @param implementationLangugage	implementation language.
     * @param majorVersion	major version.
     * @param minorVersion	minor version.
     * @param maintenanceVersion	maintenance version.
     * @param developmentFlag	development indicator flag.
     */
    public Version(String productName, String implementationLangugage,
            	   int majorVersion, int minorVersion, int maintenanceVersion,
            	   boolean developmentFlag)
    {
        //assert (productName != null);
        //assert (implementationLangugage != null);

        this.productName = productName;
        this.implementationLanguage = implementationLangugage;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.maintenanceVersion = maintenanceVersion;
        this.developmentFlag = developmentFlag;
    }

    /**
     * Get the basic version string.
     *
     * @return String denoting current version
     */
    public String getVersionString()
    {
    	String version =
    		getProductName()
                + " v"
                + getMajorVersion()
                + "."
                + getMinorVersion()
		        + "."
		        + getMaintenanceVersion();

        if (isDevelopmentVersion())
        	version += "-SNAPSHOT";

        return version;
    }

    /**
     * Name of product: Xalan.
     * @return product name.
     */
    public String getProductName()
    {
        return productName;
    }

    /**
     * Implementation Language: Java.
     * @return the implementation language.
     */
    public String getImplementationLanguage()
    {
        return implementationLanguage;
    }

    /**
     * Major version number. This changes only when there is a
     * significant, externally apparent enhancement from the previous release.
     * 'n' represents the n'th version.
     *
     * Clients should carefully consider the implications of new versions as
     * external interfaces and behaviour may have changed.
     * @return major version.
     */
    public int getMajorVersion()
    {
        return majorVersion;

    }

    /**
     * Minor vesion number. This changes when:
     * <ul>
     * <li>a new set of functionality is to be added</li>
     * <li>API or behaviour change</li>
     * <li>its designated as a reference release</li>
     * </ul>
     * @return minor version.
     */
    public int getMinorVersion()
    {
        return minorVersion;
    }

    /**
     * Maintenance version number. Optional identifier used to designate
     * maintenance drop applied to a specific release and contains fixes for
     * defects reported. It maintains compatibility with the release and
     * contains no API changes. When missing, it designates the final and
     * complete development drop for a release.
     * @return maintenance version.
     */
    public int getMaintenanceVersion()
    {
        return maintenanceVersion;
    }

    /**
     * Development flag.
     *
     * Development drops are works in progress towards a completed, final
     * release. A specific development drop may not completely implement all
     * aspects of a new feature, which may take several development drops to
     * complete. At the point of the final drop for the release, the -SNAPSHOT suffix
     * will be omitted.
     * @return development version flag.
     */
    public boolean isDevelopmentVersion()
    {
        return developmentFlag;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return getVersionString();
    }
}
