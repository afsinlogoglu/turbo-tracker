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

import com.turbo.tracker.dto.request.ProjectRequest;
import com.turbo.tracker.dto.request.ProjectUpdateRequest;
import com.turbo.tracker.dto.response.ProjectResponse;
import com.turbo.tracker.service.ProjectService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProjectController {

	private final ProjectService projects;

	public ProjectController(ProjectService projects) {
		this.projects = projects;
	}

	@GetMapping("/teams/{teamId}/projects")
	public List<ProjectResponse> list(@PathVariable Long teamId) {
		return projects.list(teamId);
	}

	@PostMapping("/teams/{teamId}/projects")
	@ResponseStatus(HttpStatus.CREATED)
	public ProjectResponse create(@PathVariable Long teamId, @Valid @RequestBody ProjectRequest request) {
		return projects.create(teamId, request);
	}

	@GetMapping("/projects/{id}")
	public ProjectResponse get(@PathVariable Long id) {
		return projects.get(id);
	}

	@PatchMapping("/projects/{id}")
	public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
		return projects.update(id, request);
	}

	@DeleteMapping("/projects/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		projects.delete(id);
	}

}
