package com.turbo.tracker.dto.request;

import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;

public class TaskUpdateRequest {

	private TaskStatus status;

	private TaskPriority priority;

	private Long assigneeId;

	private boolean assigneeTouched;

	public TaskStatus status() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public TaskPriority priority() {
		return priority;
	}

	public void setPriority(TaskPriority priority) {
		this.priority = priority;
	}

	public Long assigneeId() {
		return assigneeId;
	}

	public boolean assigneeTouched() {
		return assigneeTouched;
	}

	public void setAssigneeId(Long assigneeId) {
		this.assigneeId = assigneeId;
		this.assigneeTouched = true;
	}

}
