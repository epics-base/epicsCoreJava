/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author carcassi
 */
public class ContextSwitchBenchmark {
    
    static volatile long[] startTimesFinal;
    static volatile long[] endTimesFinal;

    public static void main(String[] args) throws Exception {
        // This benchamrk is to check how much delay is introduced by using
        // a context swith in Java.

        // Number of iterations
        int nIterations = 10000000;

        benchmarkDirectExecution(nIterations);
        benchmarkContextSwitchExecution(nIterations);
    }
    
    public static void benchmarkDirectExecution(int nIterations) throws Exception {
        long total = System.nanoTime();
        long[] startTimes = new long[nIterations];
        long[] endTimes = new long[nIterations];
        
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            int counter = 0;
            
            @Override
            public void run() {
                endTimes[counter] = System.nanoTime();
                counter++;
            }
        };
        Runnable sync = new Runnable() {
            @Override
            public void run() {
                endTimesFinal = endTimes;
            }
        };
        
        for (int i = 0; i < nIterations; i++) {
            startTimes[i] = System.nanoTime();
            task.run();
        }
        
        startTimesFinal = startTimes;
        sync.run();

        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.MINUTES);

        double avgDelay = 0;
        for (int i = 100; i < endTimes.length; i++) {
            avgDelay += (endTimesFinal[i] - startTimesFinal[i]);
        }
        avgDelay /= nIterations;
        
        System.out.println("Direct: average submission delay is " + avgDelay + " ns");
        System.out.println("Direct: average execution is " + (System.nanoTime() - total) / nIterations + " ns");
    }
    
    public static void benchmarkContextSwitchExecution(int nIterations) throws Exception {
        long total = System.nanoTime();
        long[] startTimes = new long[nIterations];
        long[] endTimes = new long[nIterations];
        
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        Runnable task = new Runnable() {
            int counter = 0;
            
            @Override
            public void run() {
                endTimes[counter] = System.nanoTime();
                counter++;
            }
        };
        Runnable sync = new Runnable() {
            @Override
            public void run() {
                endTimesFinal = endTimes;
            }
        };
        
        for (int i = 0; i < nIterations; i++) {
            startTimes[i] = System.nanoTime();
            exec.execute(task);
        }
        
        startTimesFinal = startTimes;
        exec.execute(sync);

        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.MINUTES);

        double avgDelay = 0;
        for (int i = 100; i < endTimes.length; i++) {
            avgDelay += (endTimesFinal[i] - startTimesFinal[i]);
        }
        avgDelay /= nIterations;
        
        System.out.println("Switch: average submission delay is " + avgDelay + " ns");
        System.out.println("Switch: average execution is " + (System.nanoTime() - total) / nIterations + " ns");
    }
}
