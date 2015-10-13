/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

/**
 * Factory to create a TimeFunction, which computes that average number of seconds a function call requires.
 * @author mrk
 *
 */
public class TimeFunctionFactory {
    /**
     * Create a TimeFunction.
     *
     * @param requester the function to call
     * @return the interface
     */
    public static TimeFunction create(TimeFunctionRequester requester){
        return new TimeFunctionImpl(requester);
    }
    
    private static class TimeFunctionImpl implements TimeFunction {
        private TimeFunctionRequester requester;
        
        private TimeFunctionImpl(TimeFunctionRequester requester) {
            this.requester = requester;
        }

        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.TimeFunction#timeCall()
         */
        public double timeCall() {
            double perCall = 0.0;
            long startTime,endTime;
            long ntimes = 1;
            while(true) {
                startTime = System.nanoTime();
                for(long i=0; i<ntimes; i++) requester.function();
                endTime = System.nanoTime();
                double diff = ((double)(endTime - startTime))/1e9;
                if(diff>=1.0) {
                    perCall = diff/(double)ntimes;
                    break;
                }
                ntimes *= 2;
            }
            return perCall;
        }
    }
}
