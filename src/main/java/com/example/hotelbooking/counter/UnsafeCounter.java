package com.example.hotelbooking.counter;

import org.springframework.stereotype.Component;

@Component
public class UnsafeCounter {
    private long counter = 0;

    public long increment() {
        return ++counter;
    }

    public long getValue() {
        return counter;
    }
}