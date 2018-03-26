/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Allows to submit data to be processed on another thread, allowing for
 * batch processing if there are more elements in the queue.
 *
 * @author carcassi
 */
public class ProcessingQueue<T> {
    private final Executor exec;
    private final Consumer<List<T>> batchConsumer;
    private final Object lock = new Object();
    private List<T> queue = new ArrayList<>();
    private boolean processing = false;
    
    private final Runnable processingTask = new Runnable() {
        @Override
        public void run() {
            List<T> dataToProcess;
            synchronized(lock) {
                dataToProcess = queue;
                queue = new ArrayList<>();
                processing = false;
            }
            batchConsumer.accept(dataToProcess);
        }
    };

    public ProcessingQueue(Executor exec, Consumer<List<T>> batchConsumer) {
        this.exec = exec;
        this.batchConsumer = batchConsumer;
    }
    
    public void submit(T data) {
        synchronized(lock) {
            queue.add(data);
            if (!processing) {
                exec.execute(processingTask);
                processing = true;
            }
        }
    }
}
