/**
 * Copyright (C) 2010-18 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import java.util.Objects;

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
     * Horizontal pixel offset.
     * For sub-images within a larger image,
     * this is the X offset.
     * Defaults to zero.
     *
     * @return pixel offset
     */
    public abstract int getXOffset();

    /**
     * Vertical pixel offset.
     * For sub-images within a larger image,
     * this is the Y offset.
     * Defaults to zero.
     *
     * @return pixel offset
     */
    public abstract int getYOffset();

    /**
     * Is the horizontal axis reversed?
     * Defaults to false.
     *
     * @return <code>true</code> if axis is reversed
     */
    public abstract boolean isXReversed();

    /**
     * Is the vertical axis reversed?
     * Defaults to false.
     *
     * @return <code>true</code> if axis is reversed
     */
    public abstract boolean isYReversed();

    /**
     * Image data;
     *
     * @return ListNumber image data
     */
    public abstract ListNumber getData();

    /**
     * Describes the type in which the data is stored {@link VImageDataType}
     * 
     * @return image data type
     */
    public abstract VImageDataType getDataType();

    /**
     * Returns the image type, The image type describes the mechanism in which the
     * data is encoded and how it can be converted to something that can be
     * rendered.
     * 
     * @return the image type {@link VImageType}
     */
    public abstract VImageType getVImageType();

    /**
     * Creates a new VImage. 
     * 
     * @param height image height
     * @param width image width
     * @param data image data
     * @param imageDataType image data type
     * @param vImageType image type
     * @param alarm alarm information
     * @param time timestamp
     * @return a new instance of VImage
     */
    public static VImage of(int height, int width, final ListNumber data, VImageDataType imageDataType, VImageType vImageType, Alarm alarm, Time time) {
        return of(height, width, 0, 0, false, false, data, imageDataType, vImageType, alarm, time);
    }

    /**
     * Creates a new VImage.
     *
     * @param height image height
     * @param width image width
     * @param xoffset horizontal offset
     * @param yoffset vertical offset
     * @param xreversed is horizontal axis reversed?
     * @param yreversed is vertical axis reversed?
     * @param data image data
     * @param imageDataType image data type
     * @param vImageType image type
     * @param alarm alarm information
     * @param time timestamp
     * @return a new instance of VImage
     */
    public static VImage of(int height, int width, int xoffset, int yoffset, boolean xreversed, boolean yreversed,
                            final ListNumber data, VImageDataType imageDataType, VImageType vImageType, Alarm alarm, Time time) {
        return new IVImage(height, width, xoffset, yoffset, xreversed, yreversed, data, imageDataType, vImageType, alarm, time);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof VImage) {
            VImage other = (VImage) obj;

            return getClass().equals(other.getClass()) 
                    && getHeight() == other.getHeight()
                    && getWidth() == other.getWidth()
                    && getXOffset() == other.getXOffset()
                    && getYOffset() == other.getYOffset()
                    && getData().equals(other.getData())
                    && getDataType().equals(other.getDataType())
                    && getVImageType().equals(other.getVImageType())
                    && getAlarm().equals(other.getAlarm())
                    && getTime().equals(other.getTime());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(getHeight());
        hash = 23 * hash + Objects.hashCode(getWidth());
        hash = 23 * hash + Objects.hashCode(getXOffset());
        hash = 23 * hash + Objects.hashCode(getYOffset());
        hash = 23 * hash + Objects.hashCode(getData());
        hash = 23 * hash + Objects.hashCode(getDataType());
        hash = 23 * hash + Objects.hashCode(getVImageType());
        hash = 23 * hash + Objects.hashCode(getAlarm());
        hash = 23 * hash + Objects.hashCode(getTime());
        return hash;
    }

}
