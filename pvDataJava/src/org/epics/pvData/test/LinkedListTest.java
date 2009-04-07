/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.epics.pvData.misc.LinkedList;
import org.epics.pvData.misc.LinkedListFactory;
import org.epics.pvData.misc.ListNode;
import org.epics.pvData.pv.Requester;

/**
 * JUnit test for LinkedList.
 * @author mrk
 *
 */
public class LinkedListTest extends TestCase {
    private static int numNodes = 5;
    private static LinkedList linkedList = LinkedListFactory.create();
    
    public static void testQueue() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nQueue test%n");
        for(Node node: nodes) linkedList.addTail(node.listNode);
        ListNode listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(Node node: nodes) linkedList.addTail(node.listNode);
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.println("got " + node.number);
            listNode = linkedList.removeHead();
        }
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testStack() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nStack test%n");
        for(Node node: nodes) linkedList.addHead(node.listNode);
        ListNode listNode = linkedList.removeHead();
        while(listNode!=null) listNode = linkedList.removeHead();
        for(Node node: nodes) linkedList.addHead(node.listNode);
        listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.println("got " + node.number);
            listNode = linkedList.removeHead();
        }
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testRandomInsertRemove() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nRandom insert/remove test%n");
        linkedList.addHead(nodes[4].listNode);
        linkedList.insertAfter(nodes[4].listNode, nodes[3].listNode);
        linkedList.insertAfter(nodes[3].listNode, nodes[2].listNode);
        linkedList.addTail(nodes[1].listNode);
        linkedList.addTail(nodes[0].listNode);
        ListNode listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.println("got " + node.number);
            listNode = linkedList.removeHead();
        }
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testList() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        for(Node node: nodes) linkedList.addTail(node.listNode);
        System.out.printf("%nList test%n");
        ListNode listNode = linkedList.getHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.println("got " + node.number);
            listNode = linkedList.getNext(listNode);
        }
        for(Node node: nodes) {
            listNode = node.listNode;
            if(linkedList.isOnList(listNode)) {
                linkedList.remove(listNode);
            }
        }
        assertTrue(linkedList.isEmpty());
    }
    
    public static void testOrderedQueue() {
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        System.out.printf("%nOrdered Queue test%n");
        linkedList.addHead(nodes[2].listNode);
        for(Node node: nodes) {
            ListNode thisNode = node.listNode;
            if(linkedList.isOnList(thisNode)) continue;
            ListNode listNode = linkedList.getHead();
            while(listNode!=null) {
                Node nextNode = (Node)listNode.getObject();
                if(nextNode.number>=node.number) {
                    linkedList.insertBefore(listNode, thisNode);
                    break;
                }
                listNode = linkedList.getNext(listNode);
            }
            if(linkedList.isOnList(thisNode)) continue;
            linkedList.addTail(thisNode);
        }
        ListNode listNode = linkedList.removeHead();
        while(listNode!=null) {
            Node node = (Node)listNode.getObject();
            System.out.println("got " + node.number);
            listNode = linkedList.removeHead();
        }
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
            ListNode listNode = linkedList.removeHead();
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
    
    public static void testArrayListTime() {
        numNodes = 1000;
        ArrayList<Node> arrayList = new ArrayList<Node>();
        Node[] nodes = new Node[numNodes];
        for(int i=0; i<numNodes; i++) {
            nodes[i] = new Node(i);
        }
        int ntimes = 100;
        System.out.printf("%nTime test%n");
        long beginTime = System.currentTimeMillis();
        for(int i=0; i<ntimes; i++) {
            for(Node node: nodes) arrayList.add(node);
            for(int j=0; j<nodes.length; j++) {
                Node node = arrayList.remove(0);
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
        ListNode listNode = null;
        int number = 0;
        
        
        private Node(int number) {
            this.number = number;
            listNode = LinkedListFactory.createNode(this);
        }
        
    }
}
