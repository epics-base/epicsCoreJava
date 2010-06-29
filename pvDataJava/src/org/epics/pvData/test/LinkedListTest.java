/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.epics.pvData.misc.LinkedList;
import org.epics.pvData.misc.LinkedListArray;
import org.epics.pvData.misc.LinkedListCreate;
import org.epics.pvData.misc.LinkedListNode;
import org.epics.pvData.pv.Requester;

/**
 * JUnit test for LinkedList.
 * @author mrk
 *
 */
public class LinkedListTest extends TestCase {
    private static int numNodes = 5;
    private static LinkedListCreate<Basic> basicListCreate = new LinkedListCreate<Basic>();
    private static LinkedListCreate<UserListElement> linkedListCreate = new LinkedListCreate<UserListElement>();
    //private static private static LinkedList<Node> linkedList = linkedListCreate.create();
    
    private static class Basic {
    	Basic(int i) {
    		index = i;
    	}
    	int index;
    }
    
    public static void testBasic() {
    	LinkedList<Basic> basicList = basicListCreate.create();
    	Basic[] basics = new Basic[numNodes];
    	for(int i=0; i<numNodes; i++) {
    		basics[i] = new Basic(i);
    		basicList.addTail(basicListCreate.createNode(basics[i]));
    		assertTrue(basicList.getLength()==i+1);
    	}
    	for(int i=0; i<numNodes; i++) {
    		Basic basic = basicList.getHead().getObject();
    		assert(basic.index==i);
    		basicList.remove(basics[i]);
    		int length = basicList.getLength();
    		assertTrue(length==(4-i));
    	}
    }
    
    private static class UserListElement {
        private LinkedListNode<UserListElement> listNode = null;
        private int number = 0;
        
        
        private UserListElement(int number) {
            this.number = number;
            listNode = linkedListCreate.createNode(this);
        }
        
    }
    
    public static void testQueue() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        LinkedListArray<UserListElement> linkedListArray = linkedListCreate.createArray();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        System.out.printf("%nQueue test%n");
        for(UserListElement element: elements) linkedList.addTail(element.listNode);
        LinkedListNode<UserListElement> listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(UserListElement element: elements) linkedList.addTail(element.listNode);
        LinkedListNode<UserListElement>[] linkedListNodes = null;
        int length = 0;
        synchronized(linkedList) {
            linkedListArray.setNodes(linkedList);
            linkedListNodes = linkedListArray.getNodes();
            length = linkedListArray.getLength();
        }
        System.out.printf("    LinkedListArray ");
        for(int i=0; i<length; i++) {
            System.out.printf(" " + linkedListNodes[i].getObject().number);
        }
        System.out.println();
        System.out.printf("    removeHead ");
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            UserListElement element = listNode.getObject();
            System.out.printf(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testStack() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        System.out.printf("%nStack test ");
        for(UserListElement element: elements) linkedList.addHead(element.listNode);
        LinkedListNode<UserListElement> listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(UserListElement element: elements) linkedList.addHead(element.listNode);
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            UserListElement element = listNode.getObject();
            System.out.printf(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testRandomInsertRemove() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        System.out.printf("%nRandom insert/remove test ");
        linkedList.addHead(elements[4].listNode);
        linkedList.insertAfter(elements[4].listNode, elements[3].listNode);
        linkedList.insertAfter(elements[3].listNode, elements[2].listNode);
        linkedList.addTail(elements[1].listNode);
        linkedList.addTail(elements[0].listNode);
        LinkedListNode<UserListElement> listNode = linkedList.removeHead();
        while(listNode!=null) {
            UserListElement element = listNode.getObject();
            System.out.printf(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testList() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        for(UserListElement element: elements) linkedList.addTail(element.listNode);
        System.out.printf("%nList test ");
        LinkedListNode<UserListElement> listNode = linkedList.getHead();
        while(listNode!=null) {
            UserListElement element = (UserListElement)listNode.getObject();
            System.out.printf(" " + element.number);
            listNode = linkedList.getNext(listNode);
        }
        System.out.println();
        for(UserListElement element: elements) {
            if(linkedList.contains(element)) {
                linkedList.remove(element);
            }
        }
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testOrderedQueue() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        System.out.printf("%nOrdered Queue test ");
        linkedList.addHead(elements[2].listNode);
        for(UserListElement element: elements) {
            if(linkedList.contains(element)) continue;
            LinkedListNode<UserListElement> listNode = linkedList.getHead();
            while(listNode!=null) {
                UserListElement elementOnList = listNode.getObject();
                if(elementOnList.number>=element.number) {
                    linkedList.insertBefore(listNode, element.listNode);
                    break;
                }
                listNode = linkedList.getNext(listNode);
            }
            if(linkedList.contains(element)) continue;
            linkedList.addTail(element.listNode);
        }
        LinkedListNode<UserListElement> listNode = linkedList.removeHead();
        while(listNode!=null) {
            UserListElement element = (UserListElement)listNode.getObject();
            System.out.printf(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testTime() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        numNodes = 1000;
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(UserListElement element : elements) linkedList.addTail(element.listNode);
            LinkedListNode<UserListElement> listNode = linkedList.removeHead();
            while(listNode!=null) listNode = linkedList.removeHead();
        }
        long endTime = System.currentTimeMillis();
        double diff = endTime - beginTime;
        System.out.println("diff " + diff);
        diff = diff/1000.0; // convert from milliseconds to seconds
        diff = diff/ntimes; // seconds per outer loop
        diff = diff*1e6; // converty to microseconds
        System.out.println("time per iteration " + diff + " microseconds");
        diff = diff/(numNodes*2); // convert to per addTail/removeHead
        System.out.println("time per addTail/removeHead " + diff + " microseconds");
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testTimeLocked() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        numNodes = 1000;
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime test locked%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(UserListElement element: elements) {
                synchronized(linkedList) {linkedList.addTail(element.listNode);}
            }
            while(true) {
                LinkedListNode<UserListElement> listNode = null;
                synchronized(linkedList) {listNode = linkedList.removeHead();}
                if(listNode==null) break;
            }
        }
        long endTime = System.currentTimeMillis();
        double diff = endTime - beginTime;
        System.out.println("diff " + diff);
        diff = diff/1000.0; // convert from milliseconds to seconds
        diff = diff/ntimes; // seconds per outer loop
        diff = diff*1e6; // converty to microseconds
        System.out.println("time per iteration " + diff + " microseconds");
        diff = diff/(numNodes*2); // convert to per addTail/removeHead
        System.out.println("time per addTail/removeHead " + diff + " microseconds");
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testArrayListTime() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        numNodes = 1000;
        ArrayList<UserListElement> arrayList = new ArrayList<UserListElement>();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime ArrayList test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(UserListElement element: elements) arrayList.add(element);
            while(true) {
                arrayList.remove(0);
                if(arrayList.isEmpty()) break;
            }
        }
        long endTime = System.currentTimeMillis();
        double diff = endTime - beginTime;
        System.out.println("diff " + diff);
        diff = diff/1000.0; // convert from milliseconds to seconds
        diff = diff/ntimes; // seconds per outer loop
        diff = diff*1e6; // converty to microseconds
        System.out.println("time per iteration " + diff + " microseconds");
        diff = diff/(numNodes*2); // convert to per addTail/removeHead
        System.out.println("time per addTail/removeHead " + diff + " microseconds");
        assertTrue(arrayList.isEmpty());
    }
    
    public static void testArrayListTimeLocked() {
    	LinkedList<UserListElement> linkedList = linkedListCreate.create();
        numNodes = 1000;
        ArrayList<UserListElement> arrayList = new ArrayList<UserListElement>();
        UserListElement[] elements = new UserListElement[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new UserListElement(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime ArrayList test locked%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(UserListElement element: elements) {
                synchronized(arrayList) {arrayList.add(element);}
            }
            while(true) {
                synchronized(arrayList) {
                    arrayList.remove(0);
                    if(arrayList.isEmpty()) break;
                }
            }
        }
        long endTime = System.currentTimeMillis();
        double diff = endTime - beginTime;
        System.out.println("diff " + diff);
        diff = diff/1000.0; // convert from milliseconds to seconds
        diff = diff/ntimes; // seconds per outer loop
        diff = diff*1e6; // converty to microseconds
        System.out.println("time per iteration " + diff + " microseconds");
        diff = diff/(numNodes*2); // convert to per addTail/removeHead
        System.out.println("time per addTail/removeHead " + diff + " microseconds");
        assertTrue(arrayList.isEmpty());
    }
}
