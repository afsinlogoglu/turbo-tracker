package com.turbo.tracker.dto.request;

import com.turbo.tracker.entity.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
		@NotBlank @Size(max = 120) String title,
		@Size(max = 2000) String description,
		TaskPriority priority,
		Long assigneeId) {
}
