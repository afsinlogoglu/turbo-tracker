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

import com.turbo.tracker.dto.request.MemberRequest;
import com.turbo.tracker.dto.response.MemberResponse;
import com.turbo.tracker.service.ProjectMemberService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

	private final ProjectMemberService members;

	public ProjectMemberController(ProjectMemberService members) {
		this.members = members;
	}

	@GetMapping
	public List<MemberResponse> list(@PathVariable Long projectId) {
		return members.list(projectId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MemberResponse add(@PathVariable Long projectId, @Valid @RequestBody MemberRequest request) {
		return members.add(projectId, request);
	}

	@DeleteMapping("/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remove(@PathVariable Long projectId, @PathVariable Long userId) {
		members.remove(projectId, userId);
	}

}
