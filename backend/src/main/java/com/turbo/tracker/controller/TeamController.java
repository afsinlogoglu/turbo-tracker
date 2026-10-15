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

import com.turbo.tracker.dto.request.TeamRequest;
import com.turbo.tracker.dto.request.TeamUpdateRequest;
import com.turbo.tracker.dto.response.TeamResponse;
import com.turbo.tracker.service.TeamService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

	private final TeamService teams;

	public TeamController(TeamService teams) {
		this.teams = teams;
	}

	@GetMapping
	public List<TeamResponse> list() {
		return teams.list();
	}

	@GetMapping("/{id}")
	public TeamResponse get(@PathVariable Long id) {
		return teams.get(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TeamResponse create(@Valid @RequestBody TeamRequest request) {
		return teams.create(request);
	}

	@PatchMapping("/{id}")
	public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamUpdateRequest request) {
		return teams.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		teams.delete(id);
	}

}
