package com.turbo.tracker.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "projects",
		uniqueConstraints = @UniqueConstraint(name = "uk_project_team_name", columnNames = {"team_id", "name"}),
		indexes = @Index(name = "idx_projects_team", columnList = "team_id"))
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected Project() {
	}

	public Project(Team team, String name, String description) {
		this.team = team;
		this.name = name;
		this.description = description;
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	public void rename(String name) {
		this.name = name;
	}

	public void changeDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public Team getTeam() {
		return team;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
