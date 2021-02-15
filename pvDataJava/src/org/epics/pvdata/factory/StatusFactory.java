/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.factory;

import java.nio.ByteBuffer;

import org.epics.pvdata.misc.SerializeHelper;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

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

	/**
	 * The value for NEW_LINE.
	 */
	public static String NEW_LINE = System.getProperty("line.separator");

    private static final class StatusCreateImpl implements StatusCreate {

    	private static final StatusImpl okStatus = new StatusImpl(StatusType.OK, null, null);

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StatusCreate#getStatusOK()
		 */
		public Status getStatusOK() {
			return okStatus;
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StatusCreate#createStatus(org.epics.pvdata.pv.Status.StatusType, java.lang.String, java.lang.Throwable)
		 */
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

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.StatusCreate#deserializeStatus(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
		 */
		public Status deserializeStatus(ByteBuffer buffer, DeserializableControl control) {
			control.ensureData(1);
			final byte typeCode = buffer.get();
			if (typeCode == (byte)-1)
				return okStatus;
			else {
				final String message = SerializeHelper.deserializeString(buffer, control);
				final String stackDump = SerializeHelper.deserializeString(buffer, control);
				return new StatusImpl(StatusType.values()[typeCode], message, stackDump);
			}
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
    	private StatusType type;
    	private String message;
    	private String stackDump;

		StatusImpl(StatusType type, String message, String stackDump) {
			this.type = type;
			this.message = message;
			this.stackDump = stackDump;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Status#getMessage()
		 */
		public String getMessage() {
			return message;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Status#getStackDump()
		 */
		public String getStackDump() {
			return stackDump;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Status#getType()
		 */
		public StatusType getType() {
			return type;
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Status#isOK()
		 */
		public boolean isOK() {
			return (type == StatusType.OK);
		}
		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Status#isSuccess()
		 */
		public boolean isSuccess() {
			return (type == StatusType.OK || type == StatusType.WARNING);
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

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Serializable#deserialize(java.nio.ByteBuffer, org.epics.pvdata.pv.DeserializableControl)
		 */
		public void deserialize(ByteBuffer buffer, DeserializableControl control) {
			throw new RuntimeException("use StatusCreate.deserialize()");
		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.Serializable#serialize(java.nio.ByteBuffer, org.epics.pvdata.pv.SerializableControl)
		 */
		public void serialize(ByteBuffer buffer, SerializableControl flusher) {
			flusher.ensureBuffer(1);
			if (this == getStatusCreate().getStatusOK())
			{
				// special code for okStatus (optimization)
				buffer.put((byte)-1);
			}
			else
			{
				buffer.put((byte)type.ordinal());
				SerializeHelper.serializeString(message, buffer, flusher);
				SerializeHelper.serializeString(stackDump, buffer, flusher);
			}
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((message == null) ? 0 : message.hashCode());
			result = prime * result
					+ ((stackDump == null) ? 0 : stackDump.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StatusImpl other = (StatusImpl) obj;
			if (message == null) {
				if (other.message != null)
					return false;
			} else if (!message.equals(other.message))
				return false;
			if (stackDump == null) {
				if (other.stackDump != null)
					return false;
			} else if (!stackDump.equals(other.stackDump))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

    }
}
