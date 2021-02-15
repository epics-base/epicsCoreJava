package org.epics.util.compat.legacy.net;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.DatagramChannel;

/**
 * Implementation if MembershipKey networking class
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public abstract class MembershipKey {
    /**
     * Initializes a new instance of this class.
     */
    protected MembershipKey() {
    }

    /**
     * Tells whether or not this membership is valid.
     *
     * <p> A multicast group membership is valid upon creation and remains
     * valid until the membership is dropped by invoking the {@link #drop() drop}
     * method, or the channel is closed.
     *
     * @return {@code true} if this membership key is valid, {@code false}
     * otherwise
     */
    public abstract boolean isValid();

    /**
     * Drop membership.
     *
     * <p> If the membership key represents a membership to receive all datagrams
     * then the membership is dropped and the channel will no longer receive any
     * datagrams sent to the group. If the membership key is source-specific
     * then the channel will no longer receive datagrams sent to the group from
     * that source address.
     *
     * <p> After membership is dropped it may still be possible to receive
     * datagrams sent to the group. This can arise when datagrams are waiting to
     * be received in the socket's receive buffer. After membership is dropped
     * then the channel may {@link DatagramChannel #join join} the group again
     * in which case a new membership key is returned.
     * TODO Note that DatagramChannel.join() does not exist
     *
     * <p> Upon return, this membership object will be {@link #isValid() invalid}.
     * If the multicast group membership is already invalid then invoking this
     * method has no effect. Once a multicast group membership is invalid,
     * it remains invalid forever.
     */
    public abstract void drop();

    /**
     * Block multicast datagrams from the given source address.
     *
     * <p> If this membership key is not source-specific, and the underlying
     * operating system supports source filtering, then this method blocks
     * multicast datagrams from the given source address. If the given source
     * address is already blocked then this method has no effect.
     * After a source address is blocked it may still be possible to receive
     * datagrams from that source. This can arise when datagrams are waiting to
     * be received in the socket's receive buffer.
     *
     * @param source The source address to block
     * @return This membership key
     * @throws IllegalArgumentException      If the {@code source} parameter is not a unicast address or
     *                                       is not the same address type as the multicast group
     * @throws IllegalStateException         If this membership key is source-specific or is no longer valid
     * @throws UnsupportedOperationException If the underlying operating system does not support source
     *                                       filtering
     * @throws IOException                   If an I/O error occurs
     */
    public abstract MembershipKey block(InetAddress source) throws IOException;

    /**
     * Unblock multicast datagrams from the given source address that was
     * previously blocked using the {@link #block(InetAddress) block} method.
     *
     * @param source The source address to unblock
     * @return This membership key
     * @throws IllegalStateException If the given source address is not currently blocked or the
     *                               membership key is no longer valid
     */
    public abstract MembershipKey unblock(InetAddress source);

    /**
     * Returns the channel for which this membership key was created. This
     * method will continue to return the channel even after the membership
     * becomes {@link #isValid invalid}.
     *
     * @return the channel
     */
    public abstract DatagramChannel channel();

    /**
     * Returns the multicast group for which this membership key was created.
     * This method will continue to return the group even after the membership
     * becomes {@link #isValid invalid}.
     *
     * @return the multicast group
     */
    public abstract InetAddress group();

    /**
     * Returns the network interface for which this membership key was created.
     * This method will continue to return the network interface even after the
     * membership becomes {@link #isValid invalid}.
     *
     * @return the network interface
     */
    public abstract NetworkInterface networkInterface();

    /**
     * Returns the source address if this membership key is source-specific,
     * or {@code null} if this membership is not source-specific.
     *
     * @return The source address if this membership key is source-specific,
     * otherwise {@code null}
     */
    public abstract InetAddress sourceAddress();
}
