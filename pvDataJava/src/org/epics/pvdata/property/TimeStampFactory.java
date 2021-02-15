/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.property;

/**
 * An implementation of TimeStamp.
 * @author mrk
 *
 */
public class TimeStampFactory implements TimeStamp{

	static final long milliSecPerSec = TimeStamp.milliSecPerSec;
    static final long microSecPerSec = TimeStamp.microSecPerSec;
    static final long nanoSecPerSec = TimeStamp.nanoSecPerSec;
    static final long  posixEpochAtEpicsEpoch = TimeStamp.posixEpochAtEpicsEpoch;

    private long secondsPastEpoch = 0;
    private int nanoseconds = 0;
    private int userTag = 0;

    /**
     * Create an instance of a timeStamp.
     *
     * @return the interface
     */
    public static TimeStamp create() { return new TimeStampFactory();}

    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#getUserTag()
     */
    public int getUserTag() {
		return userTag;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvdata.property.TimeStamp#setUserTag(int)
	 */
	public void setUserTag(int userTag) {
		this.userTag = userTag;
	}
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#add(double)
     */
    public void add(double seconds) {
        long secs = (long)seconds;
        long nano = (long)((seconds - secs)*1e9);
        nanoseconds += nano;
        if(nanoseconds>nanoSecPerSec) {
            nanoseconds -= nanoSecPerSec;
            secondsPastEpoch += 1;
        } else if(nanoseconds<-nanoSecPerSec) {
            nanoseconds += -nanoSecPerSec;
            secondsPastEpoch -= 1;
        }
        secondsPastEpoch += secs;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#add(long)
     */
    public void add(long seconds) {
        secondsPastEpoch += seconds;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#diff(org.epics.pvdata.property.TimeStamp, org.epics.pvdata.property.TimeStamp)
     */
    public double diff(TimeStamp a, TimeStamp b) {
        double result = a.getSecondsPastEpoch() - b.getSecondsPastEpoch();
        result += (a.getNanoseconds() - b.getNanoseconds())/1e9;
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#equals(org.epics.pvdata.property.TimeStamp)
     */
    public boolean equals(TimeStamp other) {
        long sdiff = diffInt(this,other);
        if(sdiff==0) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#le(org.epics.pvdata.property.TimeStamp)
     */
    public boolean le(TimeStamp other) {
        long sdiff = diffInt(this,other);
        if(sdiff<=0) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#lt(org.epics.pvdata.property.TimeStamp)
     */
    public boolean lt(TimeStamp other) {
        long sdiff = diffInt(this,other);
        if(sdiff<0) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#getEpicsSecondsPastEpoch()
     */
    public long getEpicsSecondsPastEpoch() {
        return secondsPastEpoch - posixEpochAtEpicsEpoch;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#getMilliSeconds()
     */
    public long getMilliSeconds() {
        return secondsPastEpoch*1000 + nanoseconds/1000000;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#getNanoseconds()
     */
    public int getNanoseconds() {
        return nanoseconds;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#getSecondsPastEpoch()
     */
    public long getSecondsPastEpoch() {
        return secondsPastEpoch;
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#normalize()
     */
    public void normalize() {
        if(nanoseconds>=0 && nanoseconds<nanoSecPerSec) return;
        while(nanoseconds>=nanoSecPerSec) {
            nanoseconds -= nanoSecPerSec;
            secondsPastEpoch++;
        }
        while(nanoseconds<0) {
            nanoseconds += nanoSecPerSec;
            secondsPastEpoch--;
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#put(long, int)
     */
    public void put(long secondsPastEpoch, int nanoseconds) {
        this.secondsPastEpoch = secondsPastEpoch;
        this.nanoseconds = nanoseconds;
        normalize();
    }
    /* (non-Javadoc)
     * @see org.epics.pvdata.property.TimeStamp#put(long)
     */
    public void put(long milliSeconds) {
        secondsPastEpoch = milliSeconds/1000;
        nanoseconds = (int)((milliSeconds%1000)*1000000);
    }

    public void getCurrentTime() {
        long currentTime = System.currentTimeMillis();
        secondsPastEpoch = currentTime/1000;
        nanoseconds = (int)((currentTime - secondsPastEpoch*1000)*1000000);
    }
    private static long diffInt(TimeStamp left,TimeStamp right ){
        long sl = left.getSecondsPastEpoch();
        int nl = left.getNanoseconds();
        long sr = right.getSecondsPastEpoch();
        int nr = right.getNanoseconds();
        long sdiff = sl - sr;
        sdiff *= nanoSecPerSec;
        sdiff += nl - nr;
        return sdiff;
    }


}
