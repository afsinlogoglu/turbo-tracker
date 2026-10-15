package com.turbo.tracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.turbo.tracker.dto.response.PageResponse;
import com.turbo.tracker.dto.response.TaskResponse;
import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;
import com.turbo.tracker.service.SearchService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

	private final SearchService search;

	public SearchController(SearchService search) {
		this.search = search;
	}

	@GetMapping("/tasks")
	public PageResponse<TaskResponse> tasks(
			@RequestParam(required = false) Long projectId,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) TaskStatus status,
			@RequestParam(required = false) TaskPriority priority,
			@RequestParam(required = false) Long assigneeId,
			@RequestParam(defaultValue = "false") boolean unassigned,
			@RequestParam(defaultValue = "newest") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return search.tasks(projectId, q, status, priority, assigneeId, unassigned, sort, page, size);
	}

}
