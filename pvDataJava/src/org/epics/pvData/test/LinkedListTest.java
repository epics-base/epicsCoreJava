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

/**
 * JUnit test for LinkedList.
 * @author mrk
 *
 */
public class LinkedListTest extends TestCase {
    private static int numNodes = 5;
    private static LinkedListCreate<Node> linkedListCreate = new LinkedListCreate<Node>();
    private static LinkedList<Node> linkedList = linkedListCreate.create();
    
    
    public static void testQueue() {
        LinkedListArray<Node> linkedListArray = linkedListCreate.createArray();
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nQueue test%n");
        for(Node node: nodes) linkedList.addTail(node.listNode);
        LinkedListNode<Node> listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(Node node: nodes) linkedList.addTail(node.listNode);
        LinkedListNode<Node>[] linkedListNodes = null;
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
            Node node = listNode.getObject();
            System.out.printf(" " + node.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testStack() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nStack test ");
        for(Node node: nodes) linkedList.addHead(node.listNode);
        LinkedListNode<Node> listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(Node node: nodes) linkedList.addHead(node.listNode);
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = listNode.getObject();
            System.out.printf(" " + node.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testRandomInsertRemove() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nRandom insert/remove test ");
        linkedList.addHead(nodes[4].listNode);
        linkedList.insertAfter(nodes[4].listNode, nodes[3].listNode);
        linkedList.insertAfter(nodes[3].listNode, nodes[2].listNode);
        linkedList.addTail(nodes[1].listNode);
        linkedList.addTail(nodes[0].listNode);
        LinkedListNode<Node> listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = listNode.getObject();
            System.out.printf(" " + node.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testList() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        for(Node node: nodes) linkedList.addTail(node.listNode);
        System.out.printf("%nList test ");
        LinkedListNode<Node> listNode = linkedList.getHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.printf(" " + node.number);
            listNode = linkedList.getNext(listNode);
        }
        System.out.println();
        for(Node node: nodes) {
            if(linkedList.contains(node)) {
                linkedList.remove(node);
            }
        }
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testOrderedQueue() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nOrdered Queue test ");
        linkedList.addHead(nodes[2].listNode);
        for(Node node: nodes) {
            if(linkedList.contains(node)) continue;
            LinkedListNode<Node> listNode = linkedList.getHead();
            while(listNode!=null) {
                Node nodeOnList = listNode.getObject();
                if(nodeOnList.number>=node.number) {
                    linkedList.insertBefore(listNode, node.listNode);
                    break;
                }
                listNode = linkedList.getNext(listNode);
            }
            if(linkedList.contains(node)) continue;
            linkedList.addTail(node.listNode);
        }
        LinkedListNode<Node> listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.printf(" " + node.number);
            listNode = linkedList.removeHead();
        }
        System.out.println();
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testTime() {
        numNodes = 1000;
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(Node node: nodes) linkedList.addTail(node.listNode);
            LinkedListNode<Node> listNode = linkedList.removeHead();
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
        numNodes = 1000;
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime test locked%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(Node node: nodes) {
                synchronized(linkedList) {linkedList.addTail(node.listNode);}
            }
            while(true) {
                LinkedListNode<Node> listNode = null;
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
        ArrayList<Node> arrayList = new ArrayList<Node>();
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        int ntimes = 1000;
        System.out.printf("%nTime ArrayList test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(Node node: nodes) arrayList.add(node);
            for(int j=0; j<nodes.length; j++) {
                arrayList.remove(0);
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
    
    private static class Node {
        private LinkedListNode<Node> listNode = null;
        private int number = 0;
        
        
        private Node(int number) {
            this.number = number;
            listNode = linkedListCreate.createNode(this);
        }
        
    }
}
