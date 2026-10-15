package com.turbo.tracker.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.turbo.tracker.dto.request.CommentRequest;
import com.turbo.tracker.dto.response.CommentResponse;
import com.turbo.tracker.service.CommentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class CommentController {

	private final CommentService comments;

	public CommentController(CommentService comments) {
		this.comments = comments;
	}

	@GetMapping("/tasks/{taskId}/comments")
	public List<CommentResponse> list(@PathVariable Long taskId) {
		return comments.list(taskId);
	}

	@PostMapping("/tasks/{taskId}/comments")
	@ResponseStatus(HttpStatus.CREATED)
	public CommentResponse create(@PathVariable Long taskId, @Valid @RequestBody CommentRequest request) {
		return comments.create(taskId, request);
	}

	@DeleteMapping("/comments/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		comments.delete(id);
	}

}
