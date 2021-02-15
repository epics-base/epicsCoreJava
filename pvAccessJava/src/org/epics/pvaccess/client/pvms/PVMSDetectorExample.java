package org.epics.pvaccess.client.pvms;

import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVByteArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;

public class PVMSDetectorExample {

	public static final Structure scanStructure =
		FieldFactory.getFieldCreate().createFieldBuilder().
			add("scanId", ScalarType.pvInt).
			add("frameSeq", ScalarType.pvInt).	// frame sequence number
			add("fps", ScalarType.pvInt).		// frames per scan
			addArray("data", ScalarType.pvByte).
			createStructure();

	public static void main(String[] args) throws Throwable
	{
		final String topicId = "DAQ";
		final String[] tags = new String[] { "detector01" };
		final InetAddress address = InetAddress.getByName("224.0.0.1");
		final int port = 5678;

		final PVMSSubscriber subscriber = new PVMSSubscriber(address, port);

		new Thread(new Runnable() {

			public void run() {
				try
				{
					PVMSSubscriber.PVMSMessage message = new PVMSSubscriber.PVMSMessage(topicId, null, null);
					while (true)
					{
						// receive with no filtering
						subscriber.receive(message, null);

						System.out.println(message.topicId);
						System.out.println(Arrays.toString(message.tags));
						System.out.println(message.data);
						System.out.println("-------");
					}
				} catch (SocketException se) {
					// noop for socket closed
				} catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}, "receiver-thread").start();


		// 8000 frames per scan
		final int FRAMES_PER_SCAN = 3; //8000;
		// 1K data per frame
		final int FRAME_DATA_SIZE = 1024;

		// create data structure
		PVStructure data = PVDataFactory.getPVDataCreate().
								createPVStructure(scanStructure);

		//
		// set static data
		//
		data.getIntField("fps").put(FRAMES_PER_SCAN);
		byte[] dataArray = new byte[FRAME_DATA_SIZE];
		PVByteArray ba = (PVByteArray)data.getScalarArrayField("data", ScalarType.pvByte);
		ba.shareData(dataArray);

		PVMSPublisher publisher = new PVMSPublisher(address, port);

		//
		// generation of 1Hz scans
		//
		int scanId = 0;
		while (true)
		{
			if (scanId == 10)
				break;

			for (int frameSeq = 0; frameSeq < FRAMES_PER_SCAN; frameSeq++)
			{
				// NOTE: field references could be cached

				data.getIntField("scanId").put(scanId);
				data.getIntField("frameSeq").put(frameSeq);

				// gen some data
				Arrays.fill(dataArray, (byte)(frameSeq % 255));

				publisher.publishData(topicId, tags, data);
			}

			Thread.sleep(1000);

			// increment scan id
			scanId++;
		}

		publisher.destroy();
		subscriber.destroy();
	}

}
