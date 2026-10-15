package com.turbo.tracker.dto.response;

import java.time.Instant;

import com.turbo.tracker.entity.Team;

public record TeamResponse(
		Long id,
		String name,
		String description,
		Instant createdAt,
		long projectCount) {

	public static TeamResponse from(Team team, long projectCount) {
		return new TeamResponse(
				team.getId(),
				team.getName(),
				team.getDescription(),
				team.getCreatedAt(),
				projectCount);
	}

}
