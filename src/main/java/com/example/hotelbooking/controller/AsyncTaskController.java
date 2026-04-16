package com.example.hotelbooking.controller;

import com.example.hotelbooking.service.AsyncTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/async")
@RequiredArgsConstructor
public class AsyncTaskController {

    private final AsyncTaskService asyncTaskService;

    @PostMapping("/notify/{bookingId}")
    public String startNotification(@PathVariable Long bookingId) {
        String taskId = asyncTaskService.startAsyncTask(bookingId);
        return "Task ID: " + taskId;
    }

    @GetMapping("/task/{taskId}")
    public String getTaskStatus(@PathVariable String taskId) {
        String status = asyncTaskService.getTaskStatus(taskId);
        return "Task " + taskId + " status: " + status;
    }
}