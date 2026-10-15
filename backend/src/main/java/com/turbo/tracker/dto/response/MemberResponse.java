package com.turbo.tracker.dto.response;

import com.turbo.tracker.entity.ProjectMember;
import com.turbo.tracker.entity.ProjectRole;

public record MemberResponse(Long userId, String name, String email, ProjectRole role) {

	public static MemberResponse from(ProjectMember member) {
		return new MemberResponse(
				member.getUser().getId(),
				member.getUser().getName(),
				member.getUser().getEmail(),
				member.getRole());
	}

}
