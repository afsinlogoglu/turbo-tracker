package com.turbo.tracker.dto.response;

import java.time.Instant;

import com.turbo.tracker.entity.Task;
import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;

public record TaskResponse(
		Long id,
		Long projectId,
		String title,
		String description,
		TaskStatus status,
		TaskPriority priority,
		Long assigneeId,
		String assigneeName,
		Instant createdAt) {

	public static TaskResponse from(Task task) {
		return new TaskResponse(
				task.getId(),
				task.getProject().getId(),
				task.getTitle(),
				task.getDescription(),
				task.getStatus(),
				task.getPriority(),
				task.getAssignee() == null ? null : task.getAssignee().getId(),
				task.getAssignee() == null ? null : task.getAssignee().getName(),
				task.getCreatedAt());
	}

}
