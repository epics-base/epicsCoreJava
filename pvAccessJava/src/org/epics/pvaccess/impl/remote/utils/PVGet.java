/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.impl.remote.utils;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.epics.pvaccess.PVAConstants;
import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.Channel.ConnectionState;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelGetRequester;
import org.epics.pvaccess.client.ChannelProvider;
import org.epics.pvaccess.client.ChannelProviderRegistryFactory;
import org.epics.pvaccess.client.ChannelRequester;
import org.epics.pvaccess.impl.remote.utils.getopt.Getopt;
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorElement;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVBoolean;
import org.epics.pvdata.pv.PVByte;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVFloat;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVLong;
import org.epics.pvdata.pv.PVScalar;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVShort;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUByte;
import org.epics.pvdata.pv.PVUInt;
import org.epics.pvdata.pv.PVULong;
import org.epics.pvdata.pv.PVUShort;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArrayData;
import org.epics.pvdata.pv.Type;
import org.epics.pvdata.pv.UnionArrayData;

/**
 * @author mse
 */
public class PVGet {

	private static final BigInteger BI_2_64 = BigInteger.ONE.shiftLeft(64);

	public static String asString(long l) {
	    return l >= 0 ? String.valueOf(l) : toBigInteger(l).toString();
	}

	public static BigInteger toBigInteger(long l) {
	    final BigInteger bi = BigInteger.valueOf(l);
	    return l >= 0 ? bi : bi.add(BI_2_64);
	}

	private static final float DEFAULT_TIMEOUT = 3.0f;	// sec
	private static final String DEFAULT_REQUEST = "field(value)";

	// TODO pvDataJava is missing output of a value only !!!
	private static void terseScalar(PrintStream o, PVScalar scalar)
	{
		switch (scalar.getScalar().getScalarType())
		{
		case pvBoolean:
			o.print(((PVBoolean)scalar).get());
			break;
		case pvByte:
			o.print(((PVByte)scalar).get());
			break;
		case pvDouble:
			o.print(((PVDouble)scalar).get());
			break;
		case pvFloat:
			o.print(((PVFloat)scalar).get());
			break;
		case pvInt:
			o.print(((PVInt)scalar).get());
			break;
		case pvLong:
			o.print(((PVLong)scalar).get());
			break;
		case pvShort:
			o.print(((PVShort)scalar).get());
			break;
		case pvString:
			o.print(((PVString)scalar).get());
			break;
		case pvUByte:
			o.print(((PVUByte)scalar).get() & 0xFF);
			break;
		case pvUInt:
			o.print(((PVUInt)scalar).get() & 0xFFFFFFFFFFFFFFFFL);
			break;
		case pvULong:
			o.print(asString(((PVULong)scalar).get()));
			break;
		case pvUShort:
			o.print(((PVUShort)scalar).get() & 0xFFFF);
			break;
		default:
			throw new RuntimeException("unsupported scalar_t");
		}
	}

	private static void terse(PrintStream o, PVField pv, char separator)
	{
		switch (pv.getField().getType())
		{
		case scalar:
			terseScalar(o, (PVScalar)pv);
			break;
		case scalarArray:
			terseScalarArray(o, (PVScalarArray)pv, separator);
			break;
		case structure:
			terseStructure(o, (PVStructure)pv, separator);
			break;
		case structureArray:
			terseStructureArray(o, (PVStructureArray)pv, separator);
			break;
		case union:
			terseUnion(o, (PVUnion)pv, separator);
			break;
		case unionArray:
			terseUnionArray(o, (PVUnionArray)pv, separator);
			break;
		default:
			throw new RuntimeException("unsupported field type");
		}
	}

	private static void terseStructure(PrintStream o, PVStructure pvStructure, char separator)
	{
		if (pvStructure == null)
		{
			o.print("(null)");
			return;
		}

		PVField[] fieldsData = pvStructure.getPVFields();
		boolean first = true;
		for (PVField fieldData : fieldsData)
		{
			if (first)
				first = false;
			else
				o.print(separator);

			terse(o, fieldData, separator);
		}
	}

	private static void terseUnion(PrintStream o, PVUnion pvUnion, char separator)
	{
		if (pvUnion == null || pvUnion.get() == null)
		{
			o.print("(null)");
			return;
		}

		terse(o, pvUnion.get(), separator);
	}

	private static void terseScalarArray(PrintStream o, PVScalarArray pvArray, char separator)
	{
		int length = pvArray.getLength();
		//final boolean arrayCountFlag = true;
		//if (arrayCountFlag)
		{
			if (length <= 0)
			{
				o.println('0');
				return;
			}
			o.print(length);
			o.print(separator);
		}

		// TODO direct access to an element is missing in pvDataJava !!!
		// convert to string array as workaround

		String[] values = new String[length];
		ConvertFactory.getConvert().toStringArray(pvArray, 0, length, values, 0);

		boolean first = true;
		for (String value : values)
		{
			if (first)
				first = false;
			else
				o.print(separator);

			o.print(value);
		}
	}

	private static void terseStructureArray(PrintStream o, PVStructureArray pvArray, char separator)
	{
		int length = pvArray.getLength();
		//final boolean arrayCountFlag = true;
		//if (arrayCountFlag)
		{
			if (length <= 0)
			{
				o.println('0');
				return;
			}
			o.print(length);
			o.print(separator);
		}

		StructureArrayData sad = new StructureArrayData();
		pvArray.get(0, length, sad);

	    boolean first = true;
	    for (PVStructure pvStructure : sad.data)
	    {
			if (first)
				first = false;
			else
				o.print(separator);

			terseStructure(o, pvStructure, separator);
	    }
	}

	private static void terseUnionArray(PrintStream o, PVUnionArray pvArray, char separator)
	{
		int length = pvArray.getLength();
		//final boolean arrayCountFlag = true;
		//if (arrayCountFlag)
		{
			if (length <= 0)
			{
				o.println('0');
				return;
			}
			o.print(length);
			o.print(separator);
		}

		UnionArrayData sad = new UnionArrayData();
		pvArray.get(0, length, sad);

	    boolean first = true;
	    for (PVUnion pvUnion : sad.data)
	    {
			if (first)
				first = false;
			else
				o.print(separator);

			terseUnion(o, pvUnion, separator);
	    }
	}

	enum PrintMode { ValueOnlyMode, StructureMode, TerseMode };

	public static void usage()
	{
	    System.err.println (
		    "\nUsage: java " + PVGet.class.getName() + " [options] <PV name>...\n\n" +
		    "  -h: Help: Print this message\n" +
		    "options:\n" +
		    "  -r <pv request>:   Request, specifies what fields to return and options, default is '" + DEFAULT_REQUEST + "'\n" +
		    "  -w <sec>:          Wait time, specifies timeout, default is " + DEFAULT_TIMEOUT + " second(s)\n" +
		    "  -t:                Terse mode - print only value, without names\n" +
	//	    "  -m:                Monitor mode\n" +
	//	    "  -q:                Quiet mode, print only error messages\n" +
		    "  -d:                Enable debug output\n" +
		    "  -F <ofs>:          Use <ofs> as an alternate output field separator\n" // +
	//	    "  -f <input file>:   Use <input file> as an input that provides a list PV name(s) to be read, use '-' for stdin\n" +
	//	    "  -c:                Wait for clean shutdown and report used instance count (for expert users)\n" +
	//	    "\nexample: pvget double01\n"
	    );
	}

    public static void main(String[] args) throws Throwable
    {

		int opt; /* getopt() current option */
		boolean debug = false;
		boolean monitor = false;
		@SuppressWarnings("unused")
		boolean quiet = false;

		float timeOut = DEFAULT_TIMEOUT;
		String request = DEFAULT_REQUEST;
		PrintMode printMode = PrintMode.ValueOnlyMode;
		char fieldSeparator = ' ';

		Getopt g = new Getopt(PVGet.class.getSimpleName(), args, ":hr:w:tmqdcF:f:");
		g.setOpterr(false);

		while ((opt = g.getopt()) != -1) {
			switch (opt) {
			case 'h': /* Print usage */
				usage();
				System.exit(0);
			case 'w': /* Set PVA timeout value */
				// TODO no error handling at all (NPE, NumberFormatException)
				timeOut = Float.valueOf(g.getOptarg());
				if (timeOut <= 0.0) {
					System.err.println(g.getOptarg()
							+ " is not a valid timeout value "
							+ "- ignored. ('PVGet -h' for help.)");
					timeOut = DEFAULT_TIMEOUT;
				}
				break;
			case 'r': /* Set PVA timeout value */
				// TODO error handling
				request = g.getOptarg();
				// do not override terse mode
				if (printMode == PrintMode.ValueOnlyMode)
					printMode = PrintMode.StructureMode;
				break;
			case 't': /* Terse mode */
				printMode = PrintMode.TerseMode;
				break;
			case 'm': /* Monitor mode */
				monitor = true;
				break;
			case 'q': /* Quiet mode */
				quiet = true;
				break;
			case 'd': /* Debug log level */
				debug = true;
				break;
			case 'F': /* Store this for output formatting */
				// TODO error handling
				fieldSeparator = g.getOptarg().charAt(0);
				break;
			case '?':
				System.err.println("Unrecognized option: '-"
						+ (char) g.getOptopt() + "'. ('PVGet -h' for help.)");
				System.exit(1);
			case ':':
				System.err.println("Option '-" + (char) g.getOptopt()
						+ "' requires an argument. ('PVGet -h' for help.)");
				System.exit(1);
			default:
				usage();
				System.exit(1);
			}
		}

		// Remaining args list are PV names
		int nPvs = args.length - g.getOptind();
		if (nPvs < 1) {
			System.err.println("No pv name(s) specified. ('PVGet -h' for help.)");
			System.exit(1);
		}

		List<String> pvs = new ArrayList<String>(nPvs);
		pvs.addAll(Arrays.asList(args).subList(g.getOptind(), args.length));

        // initialize console logging
        ConsoleLogHandler.defaultConsoleLogging(
        		debug || Integer.getInteger(PVAConstants.PVACCESS_DEBUG, 0) > 0 ? Level.ALL : Level.INFO
        				);
        Logger logger = Logger.getLogger(PVGet.class.getName());

        // setup pvAccess client
        org.epics.pvaccess.ClientFactory.start();

        // get pvAccess client provider
        ChannelProvider channelProvider =
        	ChannelProviderRegistryFactory.getChannelProviderRegistry()
        		.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);

        // create channels
        List<Channel> channels = new ArrayList<Channel>(pvs.size());
        for (String channelName : pvs)
        {
            ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger);
            Channel channel = channelProvider.createChannel(channelName, channelRequester, ChannelProvider.PRIORITY_DEFAULT);
            channels.add(channel);
        }

        CreateRequest createRequest = CreateRequest.create();
        PVStructure pvRequest = createRequest.createRequest(request);
        if (pvRequest != null)
        {
	        // do monitor
	        if (monitor)
	        {
		        CountDownLatch doneSignal = new CountDownLatch(channels.size());

		        // do get
		        for (Channel channel : channels)
		        {

			        MonitorRequester monitorRequester =
			        		new MonitorRequesterImpl(logger, channel, doneSignal, printMode, fieldSeparator);
		        	channel.createMonitor(monitorRequester, pvRequest);
		        }

		        // infinite wait
	        	doneSignal.await();
	        }
	        else
	        {
		        // do get
		        for (Channel channel : channels)
		        {
			        CountDownLatch doneSignal = new CountDownLatch(1);

			        ChannelGetRequester channelGetRequester =
			        		new ChannelGetRequesterImpl(logger, channel, doneSignal, printMode, fieldSeparator);
		        	channel.createChannelGet(channelGetRequester,pvRequest);

		        	// wait up-to 3 seconds for completion
		        	if (!doneSignal.await((long)(DEFAULT_TIMEOUT*1000), TimeUnit.MILLISECONDS))
		        		logger.info("[" + channel.getChannelName() + "] connection timeout");
		        }
	        }
        }
        else
        {
        	logger.info("createRequest failed " + createRequest.getMessage());
        }

        // stop pvAccess client
        org.epics.pvaccess.ClientFactory.stop();
    }

    private static void printData(Channel channel,
			PrintMode printMode, char fieldSeparator, PVStructure pvStructure) {
		if (printMode == PrintMode.ValueOnlyMode)
		{
			PVField value = pvStructure.getSubField("value");
			if (value == null)
			{
				System.err.println("no 'value' field");
				System.out.println(channel.getChannelName() + "\n" + pvStructure + "\n");
			}
			else
			{
				Type valueType = value.getField().getType();
				if (valueType != Type.scalar && valueType != Type.scalarArray)
				{
					// switch to structure mode
					System.out.println(channel.getChannelName() + "\n" + pvStructure + "\n");
				}
				else
				{
					if (fieldSeparator == ' ' && value.getField().getType() == Type.scalar)
						System.out.printf("%-30s", channel.getChannelName());
					else
						System.out.print(channel.getChannelName());

					System.out.print(fieldSeparator);

					terse(System.out, value, fieldSeparator);
					System.out.println();
				}
			}
		}
		else if (printMode == PrintMode.TerseMode)
		{
			terse(System.out, pvStructure, fieldSeparator);
			System.out.println();
		}
		else // if (printMode == PrintMode.StructureMode)
			System.out.println(channel.getChannelName() + "\n" + pvStructure + "\n");
	}

	static class ChannelRequesterImpl implements ChannelRequester
    {
    	private final Logger logger;
    	public ChannelRequesterImpl(Logger logger)
    	{
    		this.logger = logger;
    	}

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		public void channelCreated(Status status, Channel channel) {
			logger.fine("Channel '" + channel.getChannelName() + "' created with status: " + status + ".");
		}

		public void channelStateChange(Channel channel, ConnectionState connectionState) {
			logger.fine("Channel '" + channel.getChannelName() + "' " + connectionState + ".");
		}

    }

    static class ChannelGetRequesterImpl implements ChannelGetRequester
    {
    	private final Logger logger;
    	private final Channel channel;
    	private final CountDownLatch doneSignaler;
    	private final PrintMode printMode;
    	private final char fieldSeparator;

    	public ChannelGetRequesterImpl(Logger logger, Channel channel, CountDownLatch doneSignaler,
    			PrintMode printMode, char fieldSeparator)
    	{
    		this.logger = logger;
    		this.channel = channel;
    		this.doneSignaler = doneSignaler;
    		this.printMode = printMode;
    		this.fieldSeparator = fieldSeparator;
    	}

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		public void channelGetConnect(Status status, ChannelGet channelGet, Structure structure) {
			logger.fine("ChannelGet for '" + channel.getChannelName() + "' connected with status: " + status + ".");
			if (status.isSuccess())
			{
				channelGet.lastRequest();
				channelGet.get();
			}
			else
				doneSignaler.countDown();
		}

		public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet changedBitSet) {
			logger.fine("getDone for '" + channel.getChannelName() + "' called with status: " + status + ".");

			if (status.isSuccess())
				printData(channel, printMode, fieldSeparator, pvStructure);

			doneSignaler.countDown();
		}
    }

    static class MonitorRequesterImpl implements MonitorRequester
    {
    	private final Logger logger;
    	private final Channel channel;
    	private final CountDownLatch doneSignaler;
    	private final PrintMode printMode;
    	private final char fieldSeparator;

    	public MonitorRequesterImpl(Logger logger, Channel channel, CountDownLatch doneSignaler,
    			PrintMode printMode, char fieldSeparator)
    	{
    		this.logger = logger;
    		this.channel = channel;
    		this.doneSignaler = doneSignaler;
    		this.printMode = printMode;
    		this.fieldSeparator = fieldSeparator;
    	}

		public String getRequesterName() {
			return getClass().getName();
		}

		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		public void monitorConnect(Status status, Monitor monitor, Structure structure) {
			logger.fine("Monitor for '" + channel.getChannelName() + "' connected with status: " + status + ".");
			if (status.isSuccess())
			{
				status = monitor.start();
				if (status.isSuccess())
					return;
				else
				{
					logger.fine("Monitor::start() for '" + channel.getChannelName() + "' status: " + status + ".");
					doneSignaler.countDown();
				}
			}
			else
				doneSignaler.countDown();
		}

		public void monitorEvent(Monitor monitor) {
			MonitorElement element;
			while ((element = monitor.poll()) != null)
			{
				printData(channel, printMode, fieldSeparator, element.getPVStructure());

				monitor.release(element);
			}
		}

		public void unlisten(Monitor monitor) {
			logger.log(Level.FINE, "unlisten");
			doneSignaler.countDown();
		}

    }
}
