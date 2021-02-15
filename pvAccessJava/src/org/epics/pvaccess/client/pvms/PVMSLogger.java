package org.epics.pvaccess.client.pvms;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

public class PVMSLogger {

	public final static String TOPIC_ID = "log";

	public static final Structure timeStructure =
		FieldFactory.getFieldCreate().createFieldBuilder().
			setId("time_t").
			add("secsPastEpoch", ScalarType.pvLong).
			add("nanoseconds", ScalarType.pvInt).
			add("userTag", ScalarType.pvInt).
			createStructure();

	public static final Structure nameValue =
		FieldFactory.getFieldCreate().createFieldBuilder().
			add("name", ScalarType.pvString).
			add("value", ScalarType.pvString).
			createStructure();

	public static final Structure logStructure =
		FieldFactory.getFieldCreate().createFieldBuilder().
			setId("epics:test/NTLogRecord:1.0").

			// used to group all the logs from one "session"
			add("uuid_msb", ScalarType.pvLong).
			add("uuid_lsb", ScalarType.pvLong).

			// sequence number of a log withing session
			add("seqNo", ScalarType.pvLong).

			add("time", timeStructure).

			// TBD, for now I just pass java level
			add("level", ScalarType.pvInt).

			add("loggerName", ScalarType.pvString).

			add("message", ScalarType.pvString).
			add("host", ScalarType.pvString).
			add("process", ScalarType.pvString).
			/*
			// the following data can be sent via properties
			// they are generally useful only in case of errors for debugging,
			// however stack trace carries most of the information (excepting threadID)
			add("file", ScalarType.pvString).
			add("line", ScalarType.pvInt).
			add("method", ScalarType.pvString).
			add("source", ScalarType.pvString).	// java.util.Timer / ::std::vector
			add("threadID", ScalarType.pvInt).
			*/
			add("stackTrace", ScalarType.pvString).
			addArray("properties", nameValue).

			createStructure();

	public static class PVMSLogingHandler extends Handler
	{

		private final PVStructure log =
			PVDataFactory.getPVDataCreate().createPVStructure(logStructure);

		//private final PVLong uuid_msb = log.getSubField(PVLong.class, "uuid_msb");
		//private final PVLong uuid_lsb = log.getSubField(PVLong.class, "uuid_lsb");

		private final PVLong seqNo = log.getSubField(PVLong.class, "seqNo");

		private final PVStructure time = log.getSubField(PVStructure.class, "time");
		private final PVLong secsPastEpoch = time.getSubField(PVLong.class, "secsPastEpoch");
		private final PVInt nanoseconds = time.getSubField(PVInt.class, "nanoseconds");

		private final PVInt level = log.getSubField(PVInt.class, "level");
		private final PVString loggerName = log.getSubField(PVString.class, "loggerName");

		private final PVString message = log.getSubField(PVString.class, "message");

		// no need to send the following two fields every time,
		// they are the same for all the logs with same session uuid
		//private final PVString host = log.getSubField(PVString.class, "host");
		//private final PVString process = log.getSubField(PVString.class, "process");

		/*
		private final PVString file = log.getSubField(PVString.class, "file");
		private final PVInt line = log.getSubField(PVInt.class, "line");
		private final PVString method = log.getSubField(PVString.class, "method");
		private final PVString source = log.getSubField(PVString.class, "source");
		private final PVInt threadID = log.getSubField(PVInt.class, "threadID");
		 */

		private final PVString stackTrace = log.getSubField(PVString.class, "stackTrace");

		private final PVStructureArray properties = log.getSubField(PVStructureArray.class, "properties");

		private final AtomicLong sequenceNoCounter = new AtomicLong(0);

		private final PVMSPublisher publisher;

		private final String[] tags;

		private final StringWriter stackTraceWriter = new StringWriter();
		private final PrintWriter stackTracePrinter = new PrintWriter(stackTraceWriter);

		public PVMSLogingHandler(InetAddress sendAddress, int port) throws IOException
		{
			publisher = new PVMSPublisher(sendAddress, port);

			UUID uuid = UUID.randomUUID();
			log.getSubField(PVLong.class, "uuid_msb").put(uuid.getMostSignificantBits());
			log.getSubField(PVLong.class, "uuid_lsb").put(uuid.getLeastSignificantBits());

			time.getSubField(PVInt.class, "userTag").put(0);
			log.getSubField(PVString.class, "host").put(getHostName());

			// work only on Oracle (gives main class or Jar), but better than nothing
			String cmdLine = System.getProperty("sun.java.command", "");
			int pos = cmdLine.indexOf(' ');
			if (pos > 0)
				cmdLine = cmdLine.substring(0, pos);
			log.getSubField(PVString.class, "process").put(cmdLine);

			// tags that allow filtering (before data is actually parsed)
			// hostname, process
			tags = new String[] { getHostName(), cmdLine };

		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public synchronized void publish(LogRecord record)
		{
			if (isLoggable(record))
			{
				// reuse existing log structure

				seqNo.put(sequenceNoCounter.getAndIncrement());

				long t = record.getMillis();
				secsPastEpoch.put(t/1000);
		        nanoseconds.put((int)((t % 1000)*1000000));

		        // TODO do mapping to standardized NTLogRecord levels
				level.put(record.getLevel().intValue());

				loggerName.put(record.getLoggerName());
				message.put(record.getMessage());

				/*
				// extracting the following information from Thread.currentThread().getStackTrace() is
				// very slow, disabled
				file.put();
				line.put();
				method.put();
				source.put();

				threadID.put(record.getThreadID());
				 */

				Throwable th = record.getThrown();
				if (th != null)
				{
					th.printStackTrace(stackTracePrinter);
					stackTrace.put(stackTraceWriter.toString());

					// clear, so that the instance can be reused
					stackTraceWriter.getBuffer().setLength(0);
				}
				else
					stackTrace.put(null);

				Object[] parameters = record.getParameters();
				if (parameters != null && parameters.length > 0)
				{
					// TODO suboptimal, reuse() when new array support is there
					ArrayList<PVStructure> params = new ArrayList<PVStructure>();
					for (Object o : parameters)
						if (o instanceof Map)
						{
							Map map = (Map)o;
							Set<Map.Entry> entries = map.entrySet();
							for (Map.Entry entry : entries)
							{
								PVStructure nv = PVDataFactory.getPVDataCreate().createPVStructure(nameValue);
								nv.getSubField(PVString.class, "name").put((String)(entry.getKey()));
								nv.getSubField(PVString.class, "value").put((String)(entry.getValue()));
								params.add(nv);
							}
						}
					properties.setLength(params.size());
					properties.put(0, params.size(), params.toArray(new PVStructure[0]), 0);
				}
				else
					properties.setLength(0);

				try {
					publisher.publishData(TOPIC_ID, tags, log);
				} catch (IOException e) {
					// noop
				}
			}
		}

		@Override
		public void flush() {
			// noop
		}

		@Override
		public void close() throws SecurityException {
			publisher.destroy();
		}

		private static String hostName = null;
		private static final String HOSTNAME_KEY = "HOSTNAME";

		private static synchronized String getHostName()
		{
			if (hostName == null)
			{
				// default fallback
				hostName = "localhost";

				try {
					InetAddress localAddress = InetAddress.getLocalHost();
					hostName = localAddress.getHostName();
				} catch (Throwable uhe) {	// not only UnknownHostException
					// try with environment variable
					try {
						String envHN = System.getenv(HOSTNAME_KEY);
						if (envHN != null)
							hostName = envHN;
					} catch (Throwable th) {
						// in case not supported by JVM/OS
					}

					// and system property (overrides env. var.)
					hostName = System.getProperty(HOSTNAME_KEY, hostName);
				}
			}

			return hostName;
		}

	}

	/**
	 * Setup this handler as the only one root handler.
	 * @param logLevel root log level to be set.
	 * @param sendAddress where to send.
	 * @param port address port.
	 * @throws IOException rethrown IO exception.
	 */
	public static void installPVMSLogging(Level logLevel, InetAddress sendAddress, int port) throws IOException
	{
		PVMSLogingHandler handler = new PVMSLogingHandler(sendAddress, port);

		LogManager.getLogManager().reset();
		Logger rootLogger = Logger.getLogger("");
		rootLogger.setLevel(logLevel);
		rootLogger.addHandler(handler);
	}

	public static void main(String[] args) throws Throwable
	{
		final InetAddress address = InetAddress.getByName("224.0.0.1");
		final int port = 5678;

		installPVMSLogging(Level.INFO, address, port);


		Logger logger = Logger.getLogger("myLogger");

		logger.info("Hello world!");
		logger.finest("finest");

		Map<String, String> props = new HashMap<String, String>();
		props.put("entity", "myTwiss");
		props.put("uuid", "31");
		logger.log(Level.SEVERE, "ups, I did it again", new Object[] { props } );

		logger.log(Level.WARNING, "wow, first error", new RuntimeException("rte 1"));
		logger.log(Level.WARNING, "wow, second error", new RuntimeException("rte 2"));

	}

}
