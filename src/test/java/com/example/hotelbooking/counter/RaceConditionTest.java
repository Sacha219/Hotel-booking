package com.example.hotelbooking.counter;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RaceConditionTest {

    private static final int THREAD_COUNT = 50;
    private static final int INCREMENTS_PER_THREAD = 1000;
    private static final int EXPECTED_TOTAL = THREAD_COUNT * INCREMENTS_PER_THREAD;

    @Test
    void unsafeCounterShow() throws ReflectiveOperationException, InterruptedException {
        UnsafeCounter counter = new UnsafeCounter();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counter.increment();
                }

                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdownNow();

        long finalValue = counter.getValue();
        System.out.println("UnsafeCounter final value = " + finalValue + ", expected = " + EXPECTED_TOTAL);
        assertTrue(finalValue <= EXPECTED_TOTAL,
                "Unsafe counter should not exceed " + EXPECTED_TOTAL + " but got " + finalValue);
        assertFalse(Modifier.isSynchronized(UnsafeCounter.class.getMethod("increment").getModifiers()),
                "Unsafe counter increment should remain unsynchronized for the demonstration");
    }

    @Test
    void synchronizedCounterShow() throws InterruptedException {
        SynchronizedCounter counter = new SynchronizedCounter();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdownNow();

        long finalValue = counter.getValue();
        System.out.println("SynchronizedCounter final value = " + finalValue + ", expected = " + EXPECTED_TOTAL);
        assertEquals(EXPECTED_TOTAL, finalValue, "Synchronized counter should be correct");
    }

    @Test
    void atomicCounterShow() throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    counter.increment();
                }
                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdownNow();

        long finalValue = counter.getValue();
        System.out.println("AtomicCounter final value = " + finalValue + ", expected = " + EXPECTED_TOTAL);
        assertEquals(EXPECTED_TOTAL, finalValue, "Atomic counter should be correct");
    }
}
