/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.factory;

import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.StatusCreate;
import org.epics.pvData.pv.Status.StatusType;

/**
 * StatusFactory creates Status instances.
 * @author mse
 *
 */
public final class StatusFactory {   
    private StatusFactory(){} // don't create
    private static final StatusCreateImpl statusCreate = new StatusCreateImpl(); 
    /**
     * Get the StatusCreate interface.
     * @return The interface for creating status objects.
     */
    public static StatusCreate getStatusCreate() {
        return statusCreate;
    }
    
	public static String NEW_LINE = System.getProperty("line.separator");
	
    private static final class StatusCreateImpl implements StatusCreate {

    	private static final StatusImpl okStatus = new StatusImpl(StatusType.OK, null, null);
    	
		@Override
		public Status getStatusOK() {
			return okStatus;
		}
 
		@Override
		public Status createStatus(StatusType type, String message, Throwable cause) {
			String stackDump = null;
			if (cause != null)
			{
				StringBuffer dump = new StringBuffer(256);
				dump.append(cause.toString()).append(NEW_LINE);
	            StackTraceElement[] trace = cause.getStackTrace();
	            for (int i=0; i < trace.length; i++)
	                dump.append("\tat ").append(trace[i]).append(NEW_LINE);
	
	            Throwable ourCause = cause.getCause();
	            if (ourCause != null)
	                printStackTraceAsCause(dump, cause, ourCause);
	            stackDump = dump.toString();
			}
			
			return new StatusImpl(type, message, stackDump);
		}

    	/**
         * Print our stack trace as a cause for the specified stack trace.
         */
        private void printStackTraceAsCause(StringBuffer s, Throwable parent, Throwable cause)
        {
            // assert Thread.holdsLock(s);

            // Compute number of frames in common between this and caused
            final StackTraceElement[] trace = parent.getStackTrace();
            final StackTraceElement[] causedTrace = cause.getStackTrace();
            int m = trace.length-1, n = causedTrace.length-1;
            while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
                m--; n--;
            }
            int framesInCommon = trace.length - 1 - m;

            s.append("Caused by: ").append(cause).append(NEW_LINE);
            for (int i=0; i <= m; i++)
                s.append("\tat ").append(trace[i]).append(NEW_LINE);
            if (framesInCommon != 0)
                s.append("\t... ").append(framesInCommon).append(" more").append(NEW_LINE);

            // Recurse if we have a cause
            Throwable ourCause = cause.getCause();
            if (ourCause != null)
                printStackTraceAsCause(s, cause, ourCause);
        }

    }
    
    private static final class StatusImpl implements Status {
    	private final StatusType type;
    	private final String message;
    	private final String stackDump;
    	
		public StatusImpl(StatusType type, String message, String stackDump) {
			this.type = type;
			this.message = message;
			this.stackDump = stackDump;
		}
		@Override
		public String getMessage() {
			return message;
		}
		@Override
		public String getStackDump() {
			return stackDump;
		}
		@Override
		public StatusType getType() {
			return type;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvData.pv.Status#isOK()
		 */
		@Override
		public boolean isOK() {
			return (type == StatusType.OK);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer(30);
			buff.append("StatusImpl [type=").append(type);
			if (message != null)
				buff.append(", message=").append(message);
			if (stackDump != null)
				buff.append(", stackDump=").append(NEW_LINE).append(stackDump);
			buff.append(']');
			return buff.toString();
		}
		
		
    }
}
