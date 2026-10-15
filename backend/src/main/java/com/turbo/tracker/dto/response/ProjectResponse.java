package com.turbo.tracker.dto.response;

import java.time.Instant;

import com.turbo.tracker.entity.Project;

public record ProjectResponse(
		Long id,
		Long teamId,
		String name,
		String description,
		Instant createdAt,
		long taskCount) {

	public static ProjectResponse from(Project project, long taskCount) {
		return new ProjectResponse(
				project.getId(),
				project.getTeam().getId(),
				project.getName(),
				project.getDescription(),
				project.getCreatedAt(),
				taskCount);
	}

}
