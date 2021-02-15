package org.epics.pvaccess.client.pvms;

import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;

public class PVMSLoggerSubscriber {

	/**
	 * Line separator string.
	 */
	private static String lineSeparator = System.getProperty("line.separator");

	/**
	 * ISO 8601 date formatter.
	 */
	private static SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	/**
	 * Date object (used not to recreate it every time).
	 */
	private static Date date = new Date();

	public static String formatNTLogRecord(PVStructure log)
	{
		StringBuffer sb = new StringBuffer(128);

		PVStructure time = log.getSubField(PVStructure.class, "time");
		PVLong secsPastEpoch = time.getSubField(PVLong.class, "secsPastEpoch");
		PVInt nanoseconds = time.getSubField(PVInt.class, "nanoseconds");
		long millis = secsPastEpoch.get() * 1000 + nanoseconds.get() / 1000000;

		synchronized (date)
		{
			date.setTime(millis);
			sb.append(timeFormatter.format(date));
		}

		PVString loggerName = log.getSubField(PVString.class, "loggerName");

		// no need to send the following two fields every time,
		// they are the same for all the logs with same session uuid
		PVString host = log.getSubField(PVString.class, "host");
		PVString process = log.getSubField(PVString.class, "process");

		sb.append(' ');
		sb.append('[');
		sb.append(process.get());
		sb.append('@');
		sb.append(host.get());
		sb.append('/');
		sb.append(loggerName.get());
		sb.append(']');

		// TODO for now java level, slow parse
		PVInt level = log.getSubField(PVInt.class, "level");
		Level javaLevel = Level.parse(String.valueOf(level.get()));
		sb.append(' ');
		sb.append('[');
		sb.append(javaLevel);
		sb.append(']');

		PVString message = log.getSubField(PVString.class, "message");

		sb.append(' ');
		sb.append(message.get());
		sb.append(' ');


		PVString stackTrace = log.getSubField(PVString.class, "stackTrace");
		String stack = stackTrace.get();

		// exceptions
		if (stack != null && stack.length() > 0)
		{
			sb.append(lineSeparator);
			sb.append(stack);
		}

		return new String(sb);
	}

	public static void main(String[] args) throws Throwable
	{
		final InetAddress address = InetAddress.getByName("224.0.0.1");
		final int port = 5678;

		final PVMSSubscriber subscriber = new PVMSSubscriber(address, port);

		new Thread(new Runnable() {

			public void run() {
				try
				{
					String[] filterTags = new String[] {};

					PVMSSubscriber.PVMSMessage message = new PVMSSubscriber.PVMSMessage(PVMSLogger.TOPIC_ID, null, null);
					while (true)
					{
						subscriber.receive(message, filterTags);

						System.out.println(formatNTLogRecord((PVStructure)message.data));
					}
				} catch (SocketException se) {
					// noop for socket closed
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}, "receiver-thread").start();

	}

}
