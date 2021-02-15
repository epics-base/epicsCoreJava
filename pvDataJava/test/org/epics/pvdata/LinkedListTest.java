/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata;

import junit.framework.TestCase;
import org.epics.pvdata.misc.LinkedList;
import org.epics.pvdata.misc.LinkedListArray;
import org.epics.pvdata.misc.LinkedListCreate;
import org.epics.pvdata.misc.LinkedListNode;

import java.util.ArrayList;
import java.util.Collections;

/**
 * JUnit test for LinkedList.
 * @author mrk
 *
 */
public class LinkedListTest extends TestCase {
    private static int numNodes = 5;
    private static LinkedListCreate<Basic> basicListCreate = new LinkedListCreate<Basic>();
    private static LinkedListCreate<Element> linkedListCreate = new LinkedListCreate<Element>();
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
    		assertTrue(basic.index==i);
    		basicList.remove(basics[i]);
    		int length = basicList.getLength();
    		assertTrue(length==(numNodes-(i+1)));
    	}
    }

    private static class Element {
        private LinkedListNode<Element> listNode = null;
        private int number = 0;


        private Element(int number) {
            this.number = number;
            listNode = linkedListCreate.createNode(this);
        }

    }

    public static void testQueue() {
    	LinkedList<Element> linkedList = linkedListCreate.create();
        LinkedListArray<Element> linkedListArray = linkedListCreate.createArray();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        System.out.printf("%nQueue test%n");
        for(Element element: elements) linkedList.addTail(element.listNode);
        LinkedListNode<Element> listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(Element element: elements) linkedList.addTail(element.listNode);
        LinkedListNode<Element>[] linkedListNodes = null;
        int length = 0;
        synchronized(linkedList) {
            linkedListArray.setNodes(linkedList);
            linkedListNodes = linkedListArray.getNodes();
            length = linkedListArray.getLength();
        }
        System.out.print("    LinkedListArray ");
        for(int i=0; i<length; i++) {
            System.out.print(" " + linkedListNodes[i].getObject().number);
        }
        System.out.println();
        System.out.print("    removeHead ");
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            Element element = listNode.getObject();
            System.out.print(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }

    public static void testStack() {
    	LinkedList<Element> linkedList = linkedListCreate.create();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        System.out.printf("%nStack test ");
        for(Element element: elements) linkedList.addHead(element.listNode);
        LinkedListNode<Element> listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(Element element: elements) linkedList.addHead(element.listNode);
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            Element element = listNode.getObject();
            System.out.print(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }

    public static void testRandomInsertRemove() {
    	LinkedList<Element> linkedList = linkedListCreate.create();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        System.out.printf("%nRandom insert/remove test ");
        linkedList.addHead(elements[4].listNode);
        linkedList.insertAfter(elements[4].listNode, elements[3].listNode);
        linkedList.insertAfter(elements[3].listNode, elements[2].listNode);
        linkedList.addTail(elements[1].listNode);
        linkedList.addTail(elements[0].listNode);
        LinkedListNode<Element> listNode = linkedList.removeHead();
        while(listNode!=null) {
            Element element = listNode.getObject();
            System.out.print(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }

    public static void testList() {
    	LinkedList<Element> linkedList = linkedListCreate.create();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        for(Element element: elements) linkedList.addTail(element.listNode);
        System.out.printf("%nList test ");
        LinkedListNode<Element> listNode = linkedList.getHead();
        while(listNode!=null) {
            Element element = (Element)listNode.getObject();
            System.out.print(" " + element.number);
            listNode = linkedList.getNext(listNode);
        }
        System.out.println();
        for(Element element: elements) {
            if(linkedList.contains(element)) {
                linkedList.remove(element);
            }
        }
        assertTrue(linkedList.isEmpty());
    }

    public static void testOrderedQueue() {
    	LinkedList<Element> linkedList = linkedListCreate.create();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        System.out.printf("%nOrdered Queue test ");
        linkedList.addHead(elements[2].listNode);
        for(Element element: elements) {
            if(linkedList.contains(element)) continue;
            LinkedListNode<Element> listNode = linkedList.getHead();
            while(listNode!=null) {
                Element elementOnList = listNode.getObject();
                if(elementOnList.number>=element.number) {
                    linkedList.insertBefore(listNode, element.listNode);
                    break;
                }
                listNode = linkedList.getNext(listNode);
            }
            if(linkedList.contains(element)) continue;
            linkedList.addTail(element.listNode);
        }
        LinkedListNode<Element> listNode = linkedList.removeHead();
        while(listNode!=null) {
            Element element = (Element)listNode.getObject();
            System.out.print(" " + element.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }

    public static void testTime() {
    	LinkedList<Element> linkedList = linkedListCreate.create();
        numNodes = 1000;
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            //for(Element element : elements) linkedList.addTail(element.listNode);
            for(int j=0; j<elements.length; j++) {
                Element element = elements[j];
                linkedList.addTail(element.listNode);
            }
            LinkedListNode<Element> listNode = linkedList.removeHead();
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
    	LinkedList<Element> linkedList = linkedListCreate.create();
        numNodes = 1000;
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime test locked%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(Element element: elements) {
                synchronized(linkedList) {linkedList.addTail(element.listNode);}
            }
            while(true) {
                LinkedListNode<Element> listNode = null;
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
        numNodes = 1000;
        ArrayList<Element> arrayList = new ArrayList<Element>();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime ArrayList test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            Collections.addAll(arrayList, elements);
            do {
                arrayList.remove(0);
            } while (!arrayList.isEmpty());
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
        numNodes = 1000;
        ArrayList<Element> arrayList = new ArrayList<Element>();
        Element[] elements = new Element[numNodes];
        for(int i=0; i<numNodes; i++) {
            elements[i] = new Element(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime ArrayList test locked%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(Element element: elements) {
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
