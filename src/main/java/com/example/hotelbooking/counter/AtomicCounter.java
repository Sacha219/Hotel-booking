package com.example.hotelbooking.counter;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AtomicCounter {
    private final AtomicLong counter = new AtomicLong(0);

    public long increment() {
        return counter.incrementAndGet();
    }

    public long getValue() {
        return counter.get();
    }
}