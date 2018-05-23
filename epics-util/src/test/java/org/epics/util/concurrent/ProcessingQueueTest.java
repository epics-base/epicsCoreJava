/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import org.epics.util.stats.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ProcessingQueueTest {
    
    @Test
    public void process1() throws Exception {
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor(Executors.namedPool("test"));
        for (int i = 0; i < 100; i++) {
            List<Integer> result = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);
            Consumer<List<Integer>> consumer = new Consumer<List<Integer>>() {
                @Override
                public void accept(List<Integer> list) {
                    result.addAll(list);
                    if (result.size() == 3) {
                        latch.countDown();
                    }
                }
            };
            ProcessingQueue queue = new ProcessingQueue(exec, consumer);
            queue.submit(1);
            queue.submit(2);
            queue.submit(3);
            latch.await();
            assertThat(result, equalTo(Arrays.asList(1,2,3)));
        }
    }
    
    @Test
    public void process2() throws Exception {
        ExecutorService exec = java.util.concurrent.Executors.newSingleThreadExecutor(Executors.namedPool("test"));
        for (int i = 0; i < 100; i++) {
            List<Integer> result = new CopyOnWriteArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);
            Consumer<List<Integer>> consumer = new Consumer<List<Integer>>() {
                @Override
                public void accept(List<Integer> list) {
                    result.addAll(list);
                    if (result.size() == 3) {
                        latch.countDown();
                    }
                }
            };
            ProcessingQueue queue = new ProcessingQueue(exec, consumer);
            queue.submit(1);
            Thread.sleep(1);
            queue.submit(2);
            queue.submit(3);
            latch.await();
            assertThat(result, equalTo(Arrays.asList(1,2,3)));
        }
    }
}
