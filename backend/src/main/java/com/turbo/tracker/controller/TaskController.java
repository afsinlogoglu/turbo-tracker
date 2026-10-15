package com.turbo.tracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.turbo.tracker.dto.request.TaskRequest;
import com.turbo.tracker.dto.request.TaskUpdateRequest;
import com.turbo.tracker.dto.response.TaskResponse;
import com.turbo.tracker.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class TaskController {

	private final TaskService tasks;

	public TaskController(TaskService tasks) {
		this.tasks = tasks;
	}

	@GetMapping("/projects/{projectId}/tasks")
	public List<TaskResponse> list(@PathVariable Long projectId) {
		return tasks.list(projectId);
	}

	@PostMapping("/projects/{projectId}/tasks")
	@ResponseStatus(HttpStatus.CREATED)
	public TaskResponse create(@PathVariable Long projectId, @Valid @RequestBody TaskRequest request) {
		return tasks.create(projectId, request);
	}

	@GetMapping("/tasks/{id}")
	public TaskResponse get(@PathVariable Long id) {
		return tasks.get(id);
	}

	@PatchMapping("/tasks/{id}")
	public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
		return tasks.update(id, request);
	}

	@DeleteMapping("/tasks/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		tasks.delete(id);
	}

}
