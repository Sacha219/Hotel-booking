package com.example.hotelbooking.counter;

import org.springframework.stereotype.Component;

@Component
public class SynchronizedCounter {
    private long counter = 0;

    public synchronized long increment() {
        return ++counter;
    }

    public synchronized long getValue() {
        return counter;
    }
}