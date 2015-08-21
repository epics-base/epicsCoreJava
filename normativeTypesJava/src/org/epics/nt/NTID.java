/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.nt;
/**
 * Utility class for parsing an ID following the NT type ID conventions
 *
 * An NT type ID will be of the from epics:nt/<type-name>:<Major>.<Minor>,
 * e.g. epics:nt/NTNDArray:1.2
 * @author dgh
 */
public class NTID
{
    /**
     * Creates an NTID from the specified type ID
     *
     * @param id The the id to be parsed.
     * @return NTNDArray instance on success, null otherwise.
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
     * Get the unqualified name, without namespace or version
     *
     * For example above return "NTNDArray"
     * @return the unqualified name
     */
    public String getVersion()
    {
        if (version == null)
        {
            hasVersion = versionSepIndex >= 0;
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
        if (majorVersion == null)
        {
            if (hasVersion)
            {
                int index = fullName.indexOf('.', versionSepIndex+1);
                majorVersion = (index >= 0) 
                    ? fullName.substring(versionSepIndex+1) : "";
            }
            else
                majorVersion = "";
        }
        return majorVersion;
    }

    /**
     * Does the ID contain a major version and is it a number
     *
     * @return true if it contains a major version number
     */
    public boolean hasMajorVersion()
    {
        //TODO check it's a number
        return hasVersion;
    }

    /**
     * Get the Major version as an integer
     *
     * For example above return 1
     * @return the Major string
     */
    public int getMajorVersion()
    {

        return Integer.parseInt(getMajorVersionString());
    }

    /**
     * Get the Major version as a string
     *
     * For example above return "1"
     * @return the Major string
     */
    public String getMinorVersionString()
    {
        // TODO
        return "";
    }

    /**
     * Does the ID contain a minor version and is it a number
     *
     * @return true if it contains a minor version number
     */
    public boolean hasMinorVersion()
    {
        // TODO
        return false;
    }

    /**
     * Get the Minor version as an integer
     *
     * For example above return 1
     * @return the Minor string
     */
    public int getMinorVersion()
    {
        // TODO
        return 0;
    }

    private String fullName = null;
    private String qualifiedName = null;
    private String namespace = null;
    private String name = null;
    private String version = null;
    private String majorVersion = null;

    private boolean nsQualified = false;
    private int nsSepIndex = -1;
    private boolean hasVersion;
    private int versionSepIndex = -1; 
}
