/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.impl.remote.utils;

import java.io.PrintStream;
import java.math.BigInteger;
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
import org.epics.pvaccess.util.logging.ConsoleLogHandler;
import org.epics.pvaccess.util.logging.LoggingUtils;
import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.misc.BitSet;
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
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.StructureArrayData;
import org.epics.pvdata.pv.Type;

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
		//	break;
		case unionArray:
		//	break;
		default:
			throw new RuntimeException("unsupported field type");
		}
	}
	
	private static void terseStructure(PrintStream o, PVStructure pvStructure, char separator)
	{
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

	enum PrintMode { ValueOnlyMode, StructureMode, TerseMode };
	
    public static void main(String[] args) throws Throwable {
        
    	int argc = args.length;
        if (argc == 0 || argc > 2)
        {
            System.out.println("Usage: <channelName> [<pvRequest>]");
            return;
        }
        
        PrintMode printMode = PrintMode.ValueOnlyMode;
        
        String channelName = args[0];
        String pvRequestString = DEFAULT_REQUEST;
        
        if (argc > 1)
        {
        	pvRequestString = args[1];
        	printMode = PrintMode.StructureMode;
        }
        
        // initialize console logging
        ConsoleLogHandler.defaultConsoleLogging(Level.INFO);
        Logger logger = Logger.getLogger(PVGet.class.getName());
        
        // TODO
        if (Integer.getInteger(PVAConstants.PVACCESS_DEBUG, 0) > 0)
        	logger.setLevel(Level.ALL);

        // setup pvAccess client
        org.epics.pvaccess.ClientFactory.start();

        // get pvAccess client provider
        ChannelProvider channelProvider =
        	ChannelProviderRegistryFactory.getChannelProviderRegistry()
        		.getProvider(org.epics.pvaccess.ClientFactory.PROVIDER_NAME);
        
        //
        // create channel and channelGet
        //
        CountDownLatch doneSignal = new CountDownLatch(1);

        ChannelRequesterImpl channelRequester = new ChannelRequesterImpl(logger);
        Channel channel = channelProvider.createChannel(channelName, channelRequester, ChannelProvider.PRIORITY_DEFAULT);
        
        ChannelGetRequester channelGetRequester = new ChannelGetRequesterImpl(logger, channel, doneSignal, printMode);
        CreateRequest createRequest = CreateRequest.create();
        PVStructure pvRequest = createRequest.createRequest(pvRequestString);
        if(pvRequest==null) {
        	String message = "createRequest failed " + createRequest.getMessage();
        	logger.info(message);
        } else {
        	channel.createChannelGet(channelGetRequester,pvRequest);

        	// wait up-to 3 seconds for completion
        	if (!doneSignal.await((long)(DEFAULT_TIMEOUT*1000), TimeUnit.MILLISECONDS))
        		logger.info("[" + channel.getChannelName() + "] connection timeout");
        }
        // stop pvAccess client
        org.epics.pvaccess.ClientFactory.stop();
    }
    
    static class ChannelRequesterImpl implements ChannelRequester
    {
    	private final Logger logger;
    	public ChannelRequesterImpl(Logger logger)
    	{
    		this.logger = logger;
    	}

		@Override
		public String getRequesterName() {
			return getClass().getName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}

		@Override
		public void channelCreated(Status status, Channel channel) {
			logger.fine("Channel '" + channel.getChannelName() + "' created with status: " + status + ".");
		}
		
		@Override
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
    	
    	public ChannelGetRequesterImpl(Logger logger, Channel channel, CountDownLatch doneSignaler, PrintMode printMode)
    	{
    		this.logger = logger;
    		this.channel = channel;
    		this.doneSignaler = doneSignaler;
    		this.printMode = printMode;
    	}

		@Override
		public String getRequesterName() {
			return getClass().getName();
		}

		@Override
		public void message(String message, MessageType messageType) {
			logger.log(LoggingUtils.toLevel(messageType), message);
		}
		
		@Override
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

		@Override
		public void getDone(Status status, ChannelGet channelGet, PVStructure pvStructure, BitSet changedBitSet) {
			logger.fine("getDone for '" + channel.getChannelName() + "' called with status: " + status + ".");

			if (status.isSuccess())
			{
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
							final char fieldSeparator = ' '; 
							if (/*fieldSeparator == ' ' && */value.getField().getType() == Type.scalar)
								System.out.printf("%-30s", channel.getChannelName());
							else
								System.out.print(channel.getChannelName());
							
							System.out.print(fieldSeparator);
							
							terse(System.out, value, fieldSeparator);
						}
					}
				}
				//else if (printMode == PrintMode.TerseMode) {}
				else // if (printMode == PrintMode.StructureMode)
					System.out.println(channel.getChannelName() + "\n" + pvStructure + "\n");
			}	
			
			doneSignaler.countDown();
		}
    }
    
}
