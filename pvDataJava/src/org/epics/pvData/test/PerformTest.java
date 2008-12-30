/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.test;

import junit.framework.TestCase;

import org.epics.pvData.factory.ConvertFactory;
import org.epics.pvData.factory.FieldFactory;
import org.epics.pvData.factory.PVDataFactory;
import org.epics.pvData.pv.Convert;
import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.FieldCreate;
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
        long startTime,endTime;
        int ntimes = 10000;
        double perArray, perElement;
        startTime = System.nanoTime();
        for(int i=0; i<ntimes; i++) {
            System.arraycopy(data,0,toData,0,arraySize);
        }
        endTime = System.nanoTime();
        perArray = (double)(endTime - startTime)/(double)ntimes/1000.0;
        perElement = perArray/(double)arraySize;
        System.out.printf("data to toData perArray %f perElement %f microseconds%n",perArray,perElement);
        startTime = System.nanoTime();
        for(int i=0; i<ntimes; i++) {
            convert.copyArray(from,0,to,0,arraySize);
        }
        endTime = System.nanoTime();
        perArray = (double)(endTime - startTime)/(double)ntimes/1000.0;
        perElement = perArray/(double)arraySize;
        System.out.printf("double to double perArray %f perElement %f microseconds%n",perArray,perElement);
        startTime = System.nanoTime();
        for(int i=0; i<ntimes; i++) {
            convert.copyArray(from,0,toLong,0,arraySize);
        }
        endTime = System.nanoTime();
        perArray = (double)(endTime - startTime)/(double)ntimes/1000.0;
        perElement = perArray/(double)arraySize;
        System.out.printf("double to long perArray %f perElement %f microseconds%n",perArray,perElement);
    }
    
    public static void testCurentTime() {
        long startTime,endTime;
        int ntimes = 10000;
        startTime = System.nanoTime();
        for(int i=0; i<ntimes; i++) {
            System.currentTimeMillis();
        }
        endTime = System.nanoTime();
        double perCall = (double)(endTime - startTime)/(double)ntimes/1000.0;
        System.out.printf("currentTimeMillis %f microseconds%n",perCall);
    }

}

