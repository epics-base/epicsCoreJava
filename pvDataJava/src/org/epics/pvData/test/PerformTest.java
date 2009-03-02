/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import junit.framework.TestCase;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.misc.Executor;
import org.epics.pvData.misc.ExecutorFactory;
import org.epics.pvData.misc.ThreadPriority;
import org.epics.pvData.misc.TimeFunction;
import org.epics.pvData.misc.TimeFunctionFactory;
import org.epics.pvData.misc.TimeFunctionRequester;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
import org.epics.pvData.pv.PVArray;
import org.epics.pvData.pv.PVDataCreate;
import org.epics.pvData.pv.PVDoubleArray;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVLongArray;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.ScalarType;

/**
 * JUnit test for pvAccess.
 * It also provides examples of how to use the pvAccess interfaces.
 * @author mrk
 *
 */
public class PerformTest extends TestCase {
    private static FieldCreate fieldCreate = FieldFactory.getFieldCreate();
    private static PVDataCreate dataCreate = PVDataFactory.getPVDataCreate();
    private static Convert convert = ConvertFactory.getConvert();
    private static final int arraySize = 1000;
    /**
     * test copy array of double.
     */
    public static void testDoubleArrayCopy() {
        Field fieldFrom = fieldCreate.createArray("from",ScalarType.pvDouble);
        Field fieldTo = fieldCreate.createArray("to",ScalarType.pvDouble);
        Field fieldLong = fieldCreate.createArray("long",ScalarType.pvLong);
        Field[] fields = new Field[]{fieldFrom,fieldTo,fieldLong};
        PVRecord pvRecord = dataCreate.createPVRecord("test", fields);
        PVField[] pvDatas = pvRecord.getPVFields();
        PVDoubleArray from = (PVDoubleArray)pvDatas[0];
        PVDoubleArray to = (PVDoubleArray)pvDatas[1];
        PVLongArray toLong = (PVLongArray)pvDatas[2];
        double[] data = new double[arraySize];
        double[] toData = new double[arraySize];
        for(int i=0; i<arraySize; i++) data[i] = i;
        int nput = from.put(0,data.length,data,0);
        assertEquals(nput,arraySize);
        convert.copyArray(from, 0, to, 0, from.getLength());
        convert.copyArray(from, 0, toLong, 0, from.getLength());
        System.out.printf("%narray copy%n");
        ArrayCopy dataToData = new ArrayCopy(data,toData);
        TimeFunction timeFunction = TimeFunctionFactory.create(dataToData);
        double perCall = timeFunction.timeCall();
        System.out.printf("arrayCopy seconds per call %e per element %e%n",perCall,perCall/arraySize);
        ConvertCopy convertCopy = new ConvertCopy(from,to);
        timeFunction = TimeFunctionFactory.create(convertCopy);
        perCall = timeFunction.timeCall();
        System.out.printf("convertCopy double to double seconds per call %e per element %e%n",perCall,perCall/arraySize);
        
        convertCopy = new ConvertCopy(from,toLong);
        timeFunction = TimeFunctionFactory.create(convertCopy);
        perCall = timeFunction.timeCall();
        System.out.printf("convertCopy double to long seconds per call %e per element %e%n",perCall,perCall/arraySize);
    }
    
    private static class ArrayCopy implements TimeFunctionRequester {
        
        private ArrayCopy(double[] data ,double[] toData) {
            this.data = data;
            this.toData = toData;
        }

        /* private static class CurrentTimeFunction implements TimeFunctionRequester {(non-Javadoc)
         * @see org.epics.pvData.misc.TimeFunctionRequester#function()
         */
        public void function() {
            System.arraycopy(data,0,toData,0,data.length);
        }
        
        private double[] data;
        private double[] toData;
        
    }
    
    private static class ConvertCopy implements TimeFunctionRequester {
        
        private ConvertCopy(PVArray from ,PVArray to) {
            this.from = from;
            this.to = to;
        }

        /* private static class CurrentTimeFunction implements TimeFunctionRequester {(non-Javadoc)
         * @see org.epics.pvData.misc.TimeFunctionRequester#function()
         */
        public void function() {
            convert.copyArray(from,0,to,0,from.getLength());
        }
        
        private PVArray from;
        private PVArray to;;
        
    }
    
    public static void testCurrentTime() {
        System.out.printf("%nSystem.currentTimeMillis%n");
        CurrentTimeFunction currentTimeFunction = new CurrentTimeFunction();
        TimeFunction timeFunction = TimeFunctionFactory.create(currentTimeFunction);
        double perCall = timeFunction.timeCall();
        System.out.printf("currentTime seconds per call %e%n",perCall);
    }
    
    
    private static class CurrentTimeFunction implements TimeFunctionRequester {
        
        private CurrentTimeFunction() {}

        /* private static class CurrentTimeFunction implements TimeFunctionRequester {(non-Javadoc)
         * @see org.epics.pvData.misc.TimeFunctionRequester#function()
         */
        public void function() {
            value = System.currentTimeMillis();
        }
        
        private volatile long value = 0;
        
    }

    public static void testThreadSwitch() {
        System.out.printf("%nthreadSwitch%n");
        Executor executor = ExecutorFactory.create("testThreadSwitch", ThreadPriority.higher);
        ThreadSwitchFunction threadSwitchFunction = new ThreadSwitchFunction(executor);
        TimeFunction timeFunction = TimeFunctionFactory.create(threadSwitchFunction);
        double perCall = timeFunction.timeCall();
        System.out.printf("threadSwitch seconds per call %e%n",perCall);
        executor.stop();
        
    }
    
    private static class ThreadSwitchFunction implements TimeFunctionRequester,Runnable {
        
        private ThreadSwitchFunction(Executor executor) {
            this.executor = executor;
        }

        /* private static class CurrentTimeFunction implements TimeFunctionRequester {(non-Javadoc)
         * @see org.epics.pvData.misc.TimeFunctionRequester#function()
         */
        public void function() {
            isWaiting = true;
            executor.execute(this);
            lock.lock();
            try {
                if(isWaiting) wakeUp.await();
            } catch(InterruptedException e) {
            } finally {
                lock.unlock();
            } 
        }
            
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run() {
            lock.lock();
            try {
                isWaiting = false;
                wakeUp.signal();
            } finally {
                lock.unlock();
            }
            
        }
        private ReentrantLock lock = new ReentrantLock();
        private Condition wakeUp = lock.newCondition();
        private Executor executor;
        private boolean isWaiting = false;
    }
}

