/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import junit.framework.TestCase;

import org.epics.pvdata.copy.CreateRequest;
import org.epics.pvdata.pv.MessageType;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class CreateRequestTest extends TestCase {
    private static final Requester requester = new RequesterImpl();
    private static final CreateRequest createRequest = CreateRequest.create();

    private static class RequesterImpl implements Requester {
		public String getRequesterName() {
			return "pvCopyTest";
		}
		public void message(String message, MessageType messageType) {
		    System.out.printf("message %s messageType %s%n",message,messageType.name());

		}
    }

    /**
     *
     */
    public static void testCreateRequest() {
    	String request = "";
        PVStructure pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "record[]field()getField()putField()";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());


        request = "aaa[a=b,c=d]";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());


        request = "aaa.bbb.ccc.ddd,xxx,yyy";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "aaa{bbb{ccc{ddd}}},xxx,yyy";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "aaa[a=b,c=d].bbb[a=b,c=d].ccc[a=b,c=d].ddd[a=b,c=d],xxx[a=b,c=d],yyy[a=b,c=d]";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "aaa[a=b,c=d]{bbb[a=b,c=d]{ccc[a=b,c=d]{ddd[a=b,c=d]}}},xxx[a=b,c=d],yyy[a=b,c=d]";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "alarm,timeStamp,power.value,xxx,yyy";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());


        request = "record[process = true] field(alarm, timeStamp, power.value)";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true]field(alarm,timeStamp[algorithm=onChange,causeMonitor=false],power{value,alarm})";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "record[process=true,xxx=yyy]field(alarm,timeStamp[shareData=true],power.value)";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "record[process=true,xxx=yyy]"
        	+ "putField(power.value)"
        	+ "getField(alarm,timeStamp,power{value,alarm},"
        	+ "current{value,alarm},voltage{value,alarm})";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "field(alarm,timeStamp[algorithm=onChange,causeMonitor=false],supply[xxx=aaa,yyy=bbb]{"
                + "zero{voltage[aaa=bbb,xxx=yyy].value,current.value,power.value},"
                + "one{voltage.value[aaa=bbb,xxx=yyy],current.value,power.value}"
                + "})";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "field(alarm,timeStamp,voltage{value},power{value},current{value,alarm})";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true,xxx=yyy]"
        	+ "putField(power.value)"
        	+ "getField(alarm,timeStamp,power{value,alarm},"
        	+ "current{value,alarm},voltage{value,alarm},"
        	+ "ps0{alarm,timeStamp,power{value,alarm},current{value,alarm},voltage{value,alarm}},"
        	+ "ps1{alarm,timeStamp,power{value,alarm},current{value,alarm},voltage{value,alarm}}"
        	+ ")";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "a{b{c{d}}}";
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());

        request = "record[process=true,xxx=yyy]field(alarm,timeStamp[shareData=true],power.value";
        System.out.printf("%nError Expected for next call!!%n");
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) {
            requester.message(createRequest.getMessage(), MessageType.error);
            System.out.println(request);
        }
        assertTrue(pvRequest==null);

        request = "field(alarm.status,alarm.severity)";
        System.out.printf("%nError Expected for next call!!%n");
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) {
            requester.message(createRequest.getMessage(), MessageType.error);
            System.out.println(request);
        }
        assertTrue(pvRequest==null);

        request = "record[process=true,xxx=yyy]"
                + "putField(power.value)"
                + "getField(alarm,timeStamp,power{value,alarm},"
                + "current{value,alarm},voltage{value,alarm},"
                + "ps0{alarm,timeStamp,power{value,alarm},current{value,alarm},voltage{value,alarm}},"
                + "ps1{alarm,timeStamp,power{value,alarm},current{value,alarm},voltage{value,alarm}"
                + ")";
        System.out.printf("%nError Expected for next call!!%n");
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) {
            requester.message(createRequest.getMessage(), MessageType.error);
            System.out.println(request);
        }
        assertTrue(pvRequest==null);

        request = "record[]field()getField()putField()";
        System.out.printf("%nNo error Expected for next call!!%n");
        pvRequest = createRequest.createRequest(request);
        if(pvRequest==null) requester.message(createRequest.getMessage(), MessageType.error);
        assertTrue(pvRequest!=null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
    }
}

