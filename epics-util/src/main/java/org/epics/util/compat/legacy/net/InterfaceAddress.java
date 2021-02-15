package org.epics.util.compat.legacy.net;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * This class represents a Network Interface address. In short it's an
 * IP address, a subnet mask and a broadcast address when the address is
 * an IPv4 one. An IP address and a network prefix length in the case
 * of IPv6 address.
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 * @see java.net.NetworkInterface
 */
public class InterfaceAddress {
    private final InetAddress address;
    private final InetAddress broadcast;
    private final short maskLength;
    private final NetClass addressClass;

    public InterfaceAddress(InetAddress address) {
        this.address = address;
        this.addressClass = getAddressClass(address);
        this.broadcast = getBroadCastAddress(address, this.addressClass);
        this.maskLength = getMaskLength(address, this.addressClass);
    }

    /**
     * Get address class
     *
     * @return the address class
     */
    public NetClass getAddressClass() {
        return this.addressClass;
    }

    /**
     * Returns an <code>InetAddress</code> for this address.
     *
     * @return the <code>InetAddress</code> for this address.
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Returns an <code>InetAddress</code> for the broadcast address
     * for this InterfaceAddress.
     * <p>
     * Only IPv4 networks have broadcast address therefore, in the case
     * of an IPv6 network, <code>null</code> will be returned.
     *
     * @return the <code>InetAddress</code> representing the broadcast
     * address or <code>null</code> if there is no broadcast address.
     */
    public InetAddress getBroadcast() {
        return broadcast;
    }

    /**
     * Returns the network prefix length for this address. This is also known
     * as the subnet mask in the context of IPv4 addresses.
     * Typical IPv4 values would be 8 (255.0.0.0), 16 (255.255.0.0)
     * or 24 (255.255.255.0). <p>
     * Typical IPv6 values would be 128 (::1/128) or 10 (fe80::203:baff:fe27:1243/10)
     *
     * @return a <code>short</code> representing the prefix length for the
     * subnet of that address.
     */
    public short getNetworkPrefixLength() {
        return this.maskLength;
    }

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and it represents the same interface address as
     * this object.
     * <p>
     * Two instances of <code>InterfaceAddress</code> represent the same
     * address if the InetAddress, the prefix length and the broadcast are
     * the same for both.
     *
     * @param obj the object to compare against.
     * @return <code>true</code> if the objects are the same;
     * <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof InterfaceAddress)) {
            return false;
        }
        InterfaceAddress cmp = (InterfaceAddress) obj;
        if ((address != null & cmp.address == null) ||
                (!address.equals(cmp.address)))
            return false;
        if ((broadcast != null & cmp.broadcast == null) ||
                (!broadcast.equals(cmp.broadcast)))
            return false;
        if (maskLength != cmp.maskLength)
            return false;
        return true;
    }

    /**
     * Returns a hashcode for this Interface address.
     *
     * @return a hash code value for this Interface address.
     */
    public int hashCode() {
        return address.hashCode() + ((broadcast != null) ? broadcast.hashCode() : 0) + maskLength;
    }

    /**
     * Converts this Interface address to a <code>String</code>. The
     * string returned is of the form: InetAddress / prefix length [ broadcast address ].
     *
     * @return a string representation of this Interface address.
     */
    public String toString() {
        return address + "/" + maskLength + " [" + broadcast + "]";
    }

    private enum NetClass {
        CLASS_A,
        CLASS_B,
        CLASS_C,
        MULTICAST,
        IPV4,
        IPV6;

    }

    private short getMaskLength(InetAddress inetAddress, NetClass addressClass) {
        switch (addressClass) {
            case CLASS_A:
                return 8;
            case CLASS_B:
                return 16;
            case CLASS_C:
                return 24;
            case MULTICAST:
                byte[] address = inetAddress.getAddress();
                if (address.length == 4) {
                    return 9;
                } else {
                    return 10;
                }
            case IPV6:
                return 128;
            default:
                return 0;
        }
    }

    private NetClass getAddressClass(InetAddress inetAddress) {
        byte[] address = inetAddress.getAddress();
        int firstOctet = (address[0] & 0xFF);
        if (address.length > 4) {
            if (firstOctet == 255) {
                return NetClass.MULTICAST;
            }
            return NetClass.IPV6;
        }

        if (firstOctet <= 127) {
            return NetClass.CLASS_A;
        } else if (firstOctet <= 191) {
            return NetClass.CLASS_B;
        } else if (firstOctet <= 223) {
            return NetClass.CLASS_C;
        } else if (firstOctet <= 239) {
            return NetClass.MULTICAST;
        } else {
            return NetClass.IPV4;
        }
    }

    private InetAddress getBroadCastAddress(InetAddress inetAddress, NetClass addressClass) {
        byte[] bytes = inetAddress.getAddress();
        byte[] newBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            if (addressClass == NetClass.IPV4 || addressClass == NetClass.IPV6) {
                newBytes[i] = (byte) 255;
                continue;
            }

            switch (i) {
                case 0:
                    if (addressClass == NetClass.MULTICAST && bytes.length == 4) {
                        newBytes[i] = (byte) (bytes[i] | 0x07);
                    } else {
                        newBytes[i] = bytes[i];
                    }
                    break;
                case 1:
                    switch (addressClass) {
                        case CLASS_A:
                        case MULTICAST:
                            newBytes[i] = (byte) 255;
                            break;
                        default:
                            newBytes[i] = bytes[i];
                    }
                    break;
                case 2:
                    switch (addressClass) {
                        case CLASS_A:
                        case CLASS_B:
                        case MULTICAST:
                            newBytes[i] = (byte) 255;
                            break;
                        default:
                            newBytes[i] = bytes[i];
                    }
                    break;
                case 3:
                    switch (addressClass) {
                        case CLASS_A:
                        case CLASS_B:
                        case CLASS_C:
                        case MULTICAST:
                            newBytes[i] = (byte) 255;
                            break;
                        default:
                            newBytes[i] = bytes[i];
                    }
                    break;
                default:
                    newBytes[i] = (byte) 255;
            }
        }
        try {
            return InetAddress.getByAddress(newBytes);
        } catch (UnknownHostException ignored) {
        }
        return null;
    }

}

