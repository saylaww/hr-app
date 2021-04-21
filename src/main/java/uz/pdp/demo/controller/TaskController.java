package uz.pdp.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import uz.pdp.demo.dto.TaskDto;
import uz.pdp.demo.entity.Task;
import uz.pdp.demo.model.Response;
import uz.pdp.demo.service.TaskService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/add")
    public HttpEntity<?> add(@RequestBody TaskDto taskDto) {
        final Response response = taskService.add(taskDto);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }

    @GetMapping("/getTasks")
    public HttpEntity<?> get(@RequestParam UUID uuid) {
        final List<Task> tasks = taskService.getTasks(uuid);
        return  ResponseEntity.status(tasks != null ? 201 : 409).body(tasks);
    }

    @PostMapping("/do")
    public HttpEntity<?> doTask(@RequestParam Integer taskId, @RequestParam Integer status) {
        final Response response = taskService.doTask(taskId, status);
        return ResponseEntity.status(response.isStatus() ? 201 : 409).body(response);
    }

}
