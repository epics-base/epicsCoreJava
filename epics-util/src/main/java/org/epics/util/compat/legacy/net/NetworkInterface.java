package org.epics.util.compat.legacy.net;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Implementation of NetworkfInterface networking class
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public class NetworkInterface {
    private final java.net.NetworkInterface networkInterface;

    public java.net.NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    public NetworkInterface(java.net.NetworkInterface networkInterface) {
        this.networkInterface = networkInterface;
    }

    public int getIndex() throws SocketException {
        Enumeration<java.net.NetworkInterface> enumeratedNetworkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
        for (int i = 0; enumeratedNetworkInterfaces.hasMoreElements(); i++) {
            if (enumeratedNetworkInterfaces.nextElement().equals(this.networkInterface)) {
                return i;
            }
        }
        return -1;
    }

    public NetworkInterface getParent() {
        return null;
    }

    public boolean isUp() throws SocketException {
        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        return enumeratedInetAddresses.hasMoreElements();
    }

    public List<InterfaceAddress> getInterfaceAddresses() {
        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        List<InterfaceAddress> interfaceAddresses = new ArrayList<InterfaceAddress>();

        while (enumeratedInetAddresses.hasMoreElements()) {
            InetAddress inetAddress = enumeratedInetAddresses.nextElement();
            interfaceAddresses.add(new InterfaceAddress(inetAddress));
        }
        return Collections.unmodifiableList(interfaceAddresses);
    }

    public boolean isLoopback() {
        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        while (enumeratedInetAddresses.hasMoreElements()) {
            if (enumeratedInetAddresses.nextElement().isLoopbackAddress())
                return true;
        }
        return false;
    }

    public boolean isPointToPoint() {
        // TODO Find a good way to determine whether point to point
        return false;
    }

    public boolean supportsMulticast() {
        // TODO Find a good way to determine whether multicast.
        //  The implementation below DOES NOT WORK returns false all the time
        //  because none of the addresses bound to the NICs are multicast addresses

        Enumeration<InetAddress> enumeratedInetAddresses = this.networkInterface.getInetAddresses();
        while (enumeratedInetAddresses.hasMoreElements()) {
            if (enumeratedInetAddresses.nextElement().isMulticastAddress())
                return true;
        }
        return false;
    }

    public boolean isVirtual() {
        // TODO Find a good way to determine whether isVirtual

        return false;
    }

    public byte[] getHardwareAddress() {
        // TODO Find a good way to determine the mac code
        byte[] ipv6Address = this.networkInterface.getInetAddresses().nextElement().getAddress();
        return new byte[]{(byte) (ipv6Address[8] - 2), ipv6Address[9], ipv6Address[10], ipv6Address[13], ipv6Address[14], ipv6Address[15]};
    }

    public int getMTU() {
        // TODO Find a good way to determine the MTU
        return 1500;
    }

    public Enumeration<NetworkInterface> getSubInterfaces() {
        return Collections.enumeration(new ArrayList<NetworkInterface>());
    }

    /// DELEGATES

    public String getName() {
        return networkInterface.getName();
    }

    public Enumeration<InetAddress> getInetAddresses() {
        return networkInterface.getInetAddresses();
    }

    public String getDisplayName() {
        return networkInterface.getDisplayName();
    }

    public static NetworkInterface getByName(String s) throws SocketException {
        return new NetworkInterface(java.net.NetworkInterface.getByName(s));
    }

    public static NetworkInterface getByInetAddress(InetAddress inetAddress) throws SocketException {
        return new NetworkInterface(java.net.NetworkInterface.getByInetAddress(inetAddress));
    }

    public static Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException {
        Enumeration<java.net.NetworkInterface> enumeratedNetworkInterfaces = java.net.NetworkInterface.getNetworkInterfaces();
        List<NetworkInterface> networkInterfaceList = new ArrayList<NetworkInterface>();
        while (enumeratedNetworkInterfaces.hasMoreElements()) {
            networkInterfaceList.add(new NetworkInterface(enumeratedNetworkInterfaces.nextElement()));
        }
        return Collections.enumeration(networkInterfaceList);
    }

    @Override
    public boolean equals(Object o) {
        return networkInterface.equals(o);
    }

    @Override
    public int hashCode() {
        return networkInterface.hashCode();
    }

    @Override
    public String toString() {
        return networkInterface.toString();
    }
}
