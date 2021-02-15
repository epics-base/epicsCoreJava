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
    private final ListNumber data;
    private final VImageDataType imageDataType;
    private final VImageType imageType;

    IVImage(int height, int width, ListNumber data, VImageDataType imageDataType, VImageType imageType, Alarm alarm,
            Time time) {
        super();
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        this.alarm = alarm;
        this.time = time;
        this.height = height;
        this.width = width;
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

    public ListNumber getData() {
        return data;
    }

    public Alarm getAlarm() {
        return alarm;
    }

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
