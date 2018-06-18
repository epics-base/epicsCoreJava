/**
 * Copyright (C) 2010-18 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ListNumber;

/**
 * VImage represents an image.
 *
 * @author carcassi, shroff
 */
public abstract class VImage extends VType implements AlarmProvider, TimeProvider {

    /**
     * Height of the image in pixels.
     *
     * @return image height
     */
    public abstract int getHeight();

    /**
     * Width of the image in pixels.
     *
     * @return image width
     */
    public abstract int getWidth();

    /**
     * Image data;
     *
     * @return ListNumber image data
     */
    public abstract ListNumber getData();
    
    /**
     * Describes the type in which the data is stored
     * {@link VImageDataType}
     * 
     * @return image data type 
     */
    public abstract VImageDataType getDataType();

    /**
     * Returns the image type, The image type describes the mechanism in which
     * the data is encoded and how it can be converted to something that can be
     * rendered.
     * 
     * @return the image type {@link VImageType}
     */
    public abstract VImageType getVImageType();

    /**
     * Creates a new VImage.
     * 
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VImage of(int height, int width, final ListNumber data, VImageDataType imageDataType, Alarm alarm, Time time) {
        return new IVImage(height, width, data, imageDataType, VImageType.TYPE_3BYTE_BGR, alarm, time);
    }
}
