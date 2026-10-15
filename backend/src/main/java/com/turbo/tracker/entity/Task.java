package com.turbo.tracker.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks", indexes = @Index(name = "idx_tasks_project", columnList = "project_id"))
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(length = 2000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TaskPriority priority;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assignee_id")
	private User assignee;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Task() {
	}

	public Task(Project project, String title, String description, TaskPriority priority) {
		this.project = project;
		this.title = title;
		this.description = description;
		this.priority = priority == null ? TaskPriority.MEDIUM : priority;
		this.status = TaskStatus.TODO;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public void changeStatus(TaskStatus status) {
		if (status != null) {
			this.status = status;
		}
	}

	public void changePriority(TaskPriority priority) {
		if (priority != null) {
			this.priority = priority;
		}
	}

	public void assign(User assignee) {
		this.assignee = assignee;
	}

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public TaskPriority getPriority() {
		return priority;
	}

	public User getAssignee() {
		return assignee;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
