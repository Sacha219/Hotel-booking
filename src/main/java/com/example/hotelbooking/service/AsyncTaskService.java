package com.example.hotelbooking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AsyncTaskService {

    private final Map<String, String> taskStatus = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(1);

    public String startAsyncTask(Long bookingId) {
        String taskId = String.valueOf(taskIdGenerator.getAndIncrement());
        taskStatus.put(taskId, "IN_PROGRESS");
        log.info("Запущена асинхронная задача {} для бронирования {}", taskId, bookingId);
        processTask(taskId, bookingId);
        return taskId;
    }

    @Async
    public void processTask(String taskId, Long bookingId) {
        try {
            Thread.sleep(10000);
            taskStatus.put(taskId, "COMPLETED");
            log.info("Задача {} завершена успешно", taskId);
        } catch (InterruptedException e) {
            taskStatus.put(taskId, "FAILED");
            Thread.currentThread().interrupt();
            log.error("Задача {} прервана", taskId, e);
        }
    }

    public String getTaskStatus(String taskId) {
        return taskStatus.getOrDefault(taskId, "NOT_FOUND");
    }
}