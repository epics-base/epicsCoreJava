/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

/**
 * @author mrk
 *
 */
public class TimeStampFactory implements TimeStamp{
    
	static final long milliSecPerSec = TimeStamp.milliSecPerSec;
    static final long microSecPerSec = TimeStamp.microSecPerSec;
    static final long nanoSecPerSec = TimeStamp.nanoSecPerSec;
    static final long  posixEpochAtEpicsEpoch = TimeStamp.posixEpochAtEpicsEpoch;

    private long secondsPastEpoch = 0;
    private int nanoSeconds = 0;
    private int userTag = 0;
    
    /**
     * Create an instance of a timeStamp.
     * @return The interface.
     */
    public static TimeStamp create() { return new TimeStampFactory();}  
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#getUserTag()
     */
    @Override
	public int getUserTag() {
		return userTag;
	}
	/* (non-Javadoc)
	 * @see org.epics.pvData.property.TimeStamp#setUserTag(int)
	 */
	@Override
	public void setUserTag(int userTag) {
		this.userTag = userTag;
	}
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#add(double)
     */
    @Override
    public void add(double seconds) {
        long secs = (long)seconds;
        long nano = (long)((seconds - secs)*1e9);
        nanoSeconds += nano;
        if(nanoSeconds>nanoSecPerSec) {
            nanoSeconds -= nanoSecPerSec;
            secondsPastEpoch += 1;
        } else if(nanoSeconds<-nanoSecPerSec) {
            nanoSeconds += -nanoSecPerSec;
            secondsPastEpoch -= 1;
        }
        secondsPastEpoch += secs;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#add(long)
     */
    @Override
    public void add(long seconds) {
        secondsPastEpoch += seconds;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#diff(org.epics.pvData.property.TimeStamp, org.epics.pvData.property.TimeStamp)
     */
    @Override
    public double diff(TimeStamp a, TimeStamp b) {
        double result = a.getSecondsPastEpoch() - b.getSecondsPastEpoch();
        result += (a.getNanoSeconds() - b.getNanoSeconds())/1e9;
        return result;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#equals(org.epics.pvData.property.TimeStamp)
     */
    @Override
    public boolean equals(TimeStamp other) {
        long sdiff = diffInt(this,other);
        if(sdiff==0) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#le(org.epics.pvData.property.TimeStamp)
     */
    @Override
    public boolean le(TimeStamp other) {
        long sdiff = diffInt(this,other);
        if(sdiff<=0) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#lt(org.epics.pvData.property.TimeStamp)
     */
    @Override
    public boolean lt(TimeStamp other) {
        long sdiff = diffInt(this,other);
        if(sdiff<0) return true;
        return false;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#getEpicsSecondsPastEpoch()
     */
    @Override
    public long getEpicsSecondsPastEpoch() {
        return secondsPastEpoch - posixEpochAtEpicsEpoch;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#getMilliSeconds()
     */
    @Override
    public long getMilliSeconds() {
        return secondsPastEpoch*1000 + nanoSeconds/1000000;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#getNanoSeconds()
     */
    @Override
    public int getNanoSeconds() {
        return nanoSeconds;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#getSecondsPastEpoch()
     */
    @Override
    public long getSecondsPastEpoch() {
        return secondsPastEpoch;
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#normalize()
     */
    @Override
    public void normalize() {
        if(nanoSeconds>=0 && nanoSeconds<nanoSecPerSec) return;
        while(nanoSeconds>=nanoSecPerSec) {
            nanoSeconds -= nanoSecPerSec;
            secondsPastEpoch++;
        }
        while(nanoSeconds<0) {
            nanoSeconds += nanoSecPerSec;
            secondsPastEpoch--;
        }
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#put(long, int)
     */
    @Override
    public void put(long secondsPastEpoch, int nanoSeconds) {
        this.secondsPastEpoch = secondsPastEpoch;
        this.nanoSeconds = nanoSeconds;
        normalize();
    }
    /* (non-Javadoc)
     * @see org.epics.pvData.property.TimeStamp#put(long)
     */
    @Override
    public void put(long milliSeconds) {
        secondsPastEpoch = milliSeconds/1000;
        nanoSeconds = (int)((milliSeconds%1000)*1000000);
    }
    
    @Override
    public void getCurrentTime() {
        long currentTime = System.currentTimeMillis();
        secondsPastEpoch = currentTime/1000;
        nanoSeconds = (int)((currentTime - secondsPastEpoch*1000)*1000000);
    }
    private static long diffInt(TimeStamp left,TimeStamp right ){
        long sl = left.getSecondsPastEpoch();
        int nl = left.getNanoSeconds();
        long sr = right.getSecondsPastEpoch();
        int nr = right.getNanoSeconds();
        long sdiff = sl - sr;
        sdiff *= nanoSecPerSec;
        sdiff += nl - nr;
        return sdiff;
    }
   

}
