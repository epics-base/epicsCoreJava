package org.epics.vtype;

import org.epics.util.array.ListNumber;

/**
 * An immutable implementation of the {@link VImage}
 * 
 * @author Kunal Shroff
 *
 */
public class IVImage extends VImage {

    private final Alarm alarm;
    private final Time time;

    private final int height;
    private final int width;
    private final int xoffset;
    private final int yoffset;
    private final boolean xreversed;
    private final boolean yreversed;
    private final ListNumber data;
    private final VImageDataType imageDataType;
    private final VImageType imageType;

    IVImage(int height, int width, ListNumber data, VImageDataType imageDataType, VImageType imageType, Alarm alarm,
            Time time) {
        this(height, width, 0, 0, false, false, data, imageDataType, imageType, alarm, time);
    }

    IVImage(int height, int width, int xoffset, int yoffset, boolean xreversed, boolean yreversed,
            ListNumber data, VImageDataType imageDataType, VImageType imageType, Alarm alarm,
            Time time) {
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        this.alarm = alarm;
        this.time = time;
        this.height = height;
        this.width = width;
        this.xoffset = xoffset;
        this.yoffset = yoffset;
        this.xreversed = xreversed;
        this.yreversed = yreversed;
        this.data = data;
        this.imageDataType = imageDataType;
        this.imageType = imageType;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getXOffset() {
        return xoffset;
    }

    public int getYOffset() {
        return yoffset;
    }

    public boolean isXReversed() {
        return xreversed;
    }

    public boolean isYReversed() {
        return yreversed;
    }

    public ListNumber getData() {
        return data;
    }

    @Override
    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public VImageDataType getDataType() {
        return imageDataType;
    }

    @Override
    public VImageType getVImageType() {
        return imageType;
    }

}
