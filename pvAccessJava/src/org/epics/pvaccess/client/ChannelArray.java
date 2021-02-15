/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.pv.PVArray;

/**
 * Request to put and get Array Data.
 * The data is either taken from or put in the PVArray returned by ChannelArrayRequester.channelArrayConnect.
 * @author mrk
 */
public interface ChannelArray extends ChannelRequest{
    /**
     * put to the remote array.
     * @param putArray array to put.
     * @param offset The offset in the remote array, i.e. the PVArray returned by ChannelArrayRequester.channelArrayConnect.
     * @param count The number of elements to put, 0 means "entire array".
     * @param stride 1 means all the elements from offset to count, 2 means every other, 3 means every third, etc.
     */
    void putArray(PVArray putArray, int offset, int count, int stride);
    /**
     * get from the remote array.
     * @param offset The offset in the remote array, i.e. the PVArray returned by ChannelArrayRequester.channelArrayConnect.
     * @param count The number of elements to get, 0 means "till the end of array".
     * @param stride 1 means all the elements from offset to count, 2 means every other, 3 means every third, etc.
     */
    void getArray(int offset, int count, int stride);

    /**
     * Get the length and the capacity.
     */
    void getLength();

    /**
     * Set the length and/or the capacity.
     * @param length The new length.
     */
    void setLength(int length);
}
