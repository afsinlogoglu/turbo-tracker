package com.turbo.tracker.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "project_members",
		uniqueConstraints = @UniqueConstraint(name = "uk_member_project_user", columnNames = {"project_id", "user_id"}),
		indexes = @Index(name = "idx_members_project", columnList = "project_id"))
public class ProjectMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ProjectRole role;

	protected ProjectMember() {
	}

	public ProjectMember(Project project, User user, ProjectRole role) {
		this.project = project;
		this.user = user;
		this.role = role;
	}

	public void changeRole(ProjectRole role) {
		this.role = role;
	}

	public Long getId() {
		return id;
	}

	public Project getProject() {
		return project;
	}

	public User getUser() {
		return user;
	}

	public ProjectRole getRole() {
		return role;
	}

}
