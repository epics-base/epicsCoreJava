/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.StatusFactory;
import org.epics.pvdata.pv.DeserializableControl;
import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.SerializableControl;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Status.StatusType;
import org.epics.pvdata.pv.StatusCreate;

/**
 * JUnit test for Status.
 * @author mse
 *
 */
public class StatusTest extends TestCase {

	private static class SerializableFlushImpl implements SerializableControl {
		public void ensureBuffer(int size) {
			// TODO Auto-generated method stub

		}

		public void flushSerializeBuffer() {
			// TODO Auto-generated method stub

		}

		public void alignBuffer(int alignment) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.SerializableControl#cachedSerialize(org.epics.pvdata.pv.Field, java.nio.ByteBuffer)
		 */
		public void cachedSerialize(Field field, ByteBuffer buffer) {
			field.serialize(buffer, this);
		}

	}
	private static SerializableControl flusher = new SerializableFlushImpl();


	private static class DeserializableControlImpl implements DeserializableControl {

		public void ensureData(int size) {
			// TODO Auto-generated method stub

		}

		public void alignData(int alignment) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see org.epics.pvdata.pv.DeserializableControl#cachedDeserialize(java.nio.ByteBuffer)
		 */
		public Field cachedDeserialize(ByteBuffer buffer) {
			// no cache
			return FieldFactory.getFieldCreate().deserialize(buffer, this);
		}

	}
	private static DeserializableControl control = new DeserializableControlImpl();

	private static final StatusCreate statusCreate = StatusFactory.getStatusCreate();

	public void testStatusOK()
	{
		assertSame(statusCreate.getStatusOK(), statusCreate.getStatusOK());
	}

	public void testStatusPrints()
	{
		System.out.println(statusCreate.getStatusOK());
		System.out.println(statusCreate.createStatus(StatusType.OK, null, null));
		System.out.println(statusCreate.createStatus(StatusType.WARNING, "warning", null));
		System.out.println(statusCreate.createStatus(StatusType.ERROR, "error", new RuntimeException("simple exception")));

		try {
			throw new RuntimeException("simulated cause");
		} catch (Throwable cause) {
			try {
				throw new RuntimeException("simulated exc", cause);
			} catch (Throwable th) {
				System.out.println(statusCreate.createStatus(StatusType.FATAL, "fatal", th));
			}
		}
	}

	public void testSerializationOKStatus() {
		ByteBuffer buffer = ByteBuffer.allocate(1024);

		Status okStatus = statusCreate.getStatusOK();
		okStatus.serialize(buffer, flusher);

		buffer.flip();

		Status deserializedStatus = statusCreate.deserializeStatus(buffer, control);

		assertSame(okStatus, deserializedStatus);
	}

	public void testSerialization() {
		ByteBuffer buffer = ByteBuffer.allocate(10240);

		Status status = statusCreate.createStatus(StatusType.ERROR, "error", new RuntimeException("simple exception"));

		status.serialize(buffer, flusher);

		buffer.flip();

		Status deserializedStatus = statusCreate.deserializeStatus(buffer, control);

		assertEquals(status, deserializedStatus);
	}

}
