package com.turbo.tracker.dto.request;

import com.turbo.tracker.entity.ProjectRole;

import jakarta.validation.constraints.NotNull;

public record MemberRequest(@NotNull Long userId, ProjectRole role) {
}
