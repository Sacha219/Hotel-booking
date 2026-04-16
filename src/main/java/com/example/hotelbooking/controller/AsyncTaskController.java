package com.example.hotelbooking.controller;

import com.example.hotelbooking.service.AsyncTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    @PostMapping("/notify/{bookingId}")
    public ResponseEntity<Map<String, String>> startNotification(@PathVariable Long bookingId) {
        String taskId = asyncTaskService.startAsyncTask(bookingId);
        return ResponseEntity.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, String>> getTaskStatus(@PathVariable String taskId) {
        String status = asyncTaskService.getTaskStatus(taskId);
        return ResponseEntity.ok(Map.of("status", status));
    }
}