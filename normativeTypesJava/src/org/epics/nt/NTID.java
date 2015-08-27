/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.nt;
/**
 * Utility class for parsing an ID following the NT type ID conventions
 *
 * An NT type ID will be of the from epics:nt/&lt;type-name&gt;:&lt;Major&gt;.&lt;Minor&gt;,
 * e.g. epics:nt/NTNDArray:1.2
 * @author dgh
 */
public class NTID
{
    /**
     * Creates an NTID from the specified type ID
     *
     * @param id The the id to be parsed.
     */
    public NTID(String id)
    {
        fullName = id;

        nsSepIndex = id.indexOf('/');
        nsQualified = nsSepIndex >=0;

        int startIndex = nsQualified ? nsSepIndex+1 : 0;
        versionSepIndex = id.indexOf(':', startIndex);
        hasVersion = versionSepIndex >= 0;
    }

    /**
     * Get the full name of the id, i.e. the original ID
     *
     * For example above returns "epics:nt/NTNDArray:1.2"
     * @return the full name
     */
    public String getFullName() { return fullName; }

    /**
     * Get the fully qualified name including namespaces, but excluding version numbers
     *
     * For example above return "epics:nt/NTNDArray"
     * @return the fully qualified name
     */ 
    public String getQualifiedName()
    {
        if (qualifiedName == null)
        {
            qualifiedName = hasVersion ?
                fullName.substring(0, versionSepIndex) : fullName;
        }
        return qualifiedName;
    }

    /**
     * Get the namespace
     *
     * For example above return "epics:nt"
     * @return the namespace
     */
    public String getNamespace()
    {
        if (namespace == null)
        {
            namespace = nsQualified ?
               fullName.substring(0, nsSepIndex) : "";
        }
        return namespace;
    }

    /**
     * Get the unqualified name, without namespace or version
     *
     * For example above return "NTNDArray"
     * @return the unqualified name
     */
    public String getName()
    {
        if (name == null)
        {
            if (hasVersion)
            {
                name = fullName.substring(nsQualified ? nsSepIndex+1 : 0,
                      versionSepIndex);
            }
            else if (nsQualified)
            {
                name = fullName.substring(nsSepIndex+1);
            }
            else
            {
                name = fullName;
            } 
        }
        return name;
    }

    /**
     * Get the version number as a string
     *
     * For example above return "1.2"
     * @return the unqualified name
     */
    public String getVersion()
    {
        if (version == null)
        {
            version = (hasVersion) ? fullName.substring(versionSepIndex+1) : "";
        }
        return version;
    }

    /**
     * Get the Major version as a string
     *
     * For example above return "1"
     * @return the Major string
     */
    public String getMajorVersionString()
    {
        if (majorVersionStr == null)
        {
            if (hasVersion)
            {
                endMajorIndex = fullName.indexOf('.', versionSepIndex+1);
                majorVersionStr = (endMajorIndex >= 0) 
                    ? fullName.substring(versionSepIndex+1, endMajorIndex) : 
                      fullName.substring(versionSepIndex+1);
            }
            else
                majorVersionStr = "";
        }
        return majorVersionStr;
    }

    /**
     * Does the ID contain a major version and is it a number
     *
     * @return true if it contains a major version number
     */
    public boolean hasMajorVersion()
    {
        if (hasVersion && !majorVersionParsed)
        {
            try {
                majorVersion = Integer.parseInt(getMajorVersionString());
                hasMajor = true;
            } catch (NumberFormatException e) {}
            majorVersionParsed = true;
        }
        return hasMajor;
    }

    /**
     * Get the Major version as an integer
     *
     * For example above return 1
     * If hasMajorVersion() returns true then this method returns
     * the integer version number, else it returns 0. 
     * @return the Major version
     */
    public int getMajorVersion()
    {
        // call hasMajorVersion() to calculate values
        hasMajorVersion();
        return majorVersion;
    }

    /**
     * Get the Minor version as a string
     *
     * For example above return "2"
     * @return the Minor string
     */
    public String getMinorVersionString()
    {
        // call hasMinorVersion() to calculate start of minor
        getMajorVersionString();
        if (minorVersionStr == null)
        {
            if (hasVersion && endMajorIndex >= 0)
            {
                endMinorIndex = fullName.indexOf('.', endMajorIndex+1);
                minorVersionStr = (endMinorIndex >= 0) 
                    ? fullName.substring(endMajorIndex+1, endMinorIndex) : 
                      fullName.substring(endMajorIndex+1);
            }
            else
                minorVersionStr = "";
        }
        return minorVersionStr;
    }

    /**
     * Does the ID contain a minor version and is it a number
     *
     * @return true if it contains a minor version number
     */
    public boolean hasMinorVersion()
    {
        if (hasVersion && !minorVersionParsed)
        {
            try {
                minorVersion = Integer.parseInt(getMinorVersionString());
                hasMinor = true;
            } catch (NumberFormatException e) {}
            minorVersionParsed = true;
        }
        return hasMinor;
    }

    /**
     * Get the Minor version as an integer
     *
     * For example above return 2
     * If hasMinorVersion() returns true then this method returns
     * the integer version number, else it returns 0. 
     * @return the Minor string
     */
    public int getMinorVersion()
    {
        // call hasMinorVersion() to calculate values
        hasMinorVersion();
        return minorVersion;
    }

    private String fullName = null;
    private String qualifiedName = null;
    private String namespace = null;
    private String name = null;
    private String version = null;

    private int nsSepIndex = -1;
    private int versionSepIndex = -1;
    private boolean nsQualified = false;
    private boolean hasVersion = false;

    private int endMajorIndex = 0;
    private String majorVersionStr = null;
    private boolean majorVersionParsed;
    private boolean hasMajor = false;
    private int majorVersion = 0;

    private int endMinorIndex = 0;
    private String minorVersionStr = null;
    private boolean minorVersionParsed;
    private boolean hasMinor = false;
    private int minorVersion = 0;
}
