/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy.test;

import junit.framework.TestCase;

import org.epics.pvData.pv.MessageType;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pvCopy.PVCopyFactory;



/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PVCreateRequestTest extends TestCase {
    private static final Requester requester = new RequesterImpl();
    
    private static class RequesterImpl implements Requester {
		@Override
		public String getRequesterName() {
			return "pvCopyTest";
		}
		@Override
		public void message(String message, MessageType messageType) {
		    System.out.printf("message %s messageType %s%n",message,messageType.name());
			
		}
    }
    
    public static void testCreateRequest() {
    	String request = "";
        PVStructure pvRequest = PVCopyFactory.createRequest(request,requester);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "alarm,timeStamp,power.value";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true]field(alarm,timeStamp,power.value)";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true]field(alarm,timeStamp[algorithm=onChange,causeMonitor=false],power{power.value,power.alarm})";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true,xxx=yyy]field(alarm,timeStamp[shareData=true],power.value)";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true,xxx=yyy]"
        	+ "putField(power.value)"
        	+ "getField(alarm,timeStamp,power{power.value,power.alarm},"
        	+ "current{current.value,current.alarm},voltage{voltage.value,voltage.alarm})";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true,xxx=yyy]"
        	+ "putField(power.value)"
        	+ "getField(alarm,timeStamp,power{power.value,power.alarm},"
        	+ "current{current.value,current.alarm},voltage{voltage.value,voltage.alarm},"
        	+ "ps0{"
        	+ "ps0.alarm,ps0.timeStamp,power{ps0.power.value,ps0.power.alarm},"
        	+ "current{ps0.current.value,ps0.current.alarm},voltage{ps0.voltage.value,ps0.voltage.alarm}},"
        	+ "ps1{"
        	+ "ps1.alarm,ps1.timeStamp,power{ps1.power.value,ps1.power.alarm},"
        	+ "current{ps1.current.value,ps1.current.alarm},voltage{ps1.voltage.value,ps1.voltage.alarm}"
        	+ "})";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "a{b{c{d}}}";
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertFalse(pvRequest==null);
        System.out.printf("request %s%n%s%n",request,pvRequest.toString());
        request = "record[process=true,xxx=yyy]field(alarm,timeStamp[shareData=true],power.value";
        System.out.printf("%nError Expected for next call!!%n");
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertTrue(pvRequest==null);
        request = "record[process=true,xxx=yyy]"
        	+ "putField(power.value)"
        	+ "getField(alarm,timeStamp,power{power.value,power.alarm},"
        	+ "current{current.value,current.alarm},voltage{voltage.value,voltage.alarm},"
        	+ "ps0{"
        	+ "ps0.alarm,ps0.timeStamp,power{ps0.power.value,ps0.power.alarm},"
        	+ "current{ps0.current.value,ps0.current.alarm},voltage{ps0.voltage.value,ps0.voltage.alarm}},"
        	+ "ps1{"
        	+ "ps1.alarm,ps1.timeStamp,power{ps1.power.value,ps1.power.alarm},"
        	+ "current{ps1.current.value,ps1.current.alarm},voltage{ps1.voltage.value,ps1.voltage.alarm}"
        	+ ")";
        System.out.printf("%nError Expected for next call!!%n");
        pvRequest = PVCopyFactory.createRequest(request,requester);
        assertTrue(pvRequest==null);
    }
}

