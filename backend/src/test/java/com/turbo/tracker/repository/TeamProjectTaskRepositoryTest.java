package com.turbo.tracker.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.entity.Project;
import com.turbo.tracker.entity.Task;
import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;
import com.turbo.tracker.entity.Team;

@SpringBootTest(properties = "turbo.seed.enabled=false")
@Transactional
class TeamProjectTaskRepositoryTest {

	@Autowired
	private TeamRepository teams;

	@Autowired
	private ProjectRepository projects;

	@Autowired
	private TaskRepository tasks;

	@Test
	void linksATaskToItsProjectAndTeam() {
		Team team = teams.save(new Team("Platform", "Backend team"));
		Project project = projects.save(new Project(team, "Turbo API", "REST for teams"));
		Task task = tasks.save(new Task(project, "Add team endpoint", "GET /api/teams", TaskPriority.HIGH));

		Task found = tasks.findById(task.getId()).orElseThrow();

		assertThat(found.getProject().getId()).isEqualTo(project.getId());
		assertThat(found.getProject().getTeam().getName()).isEqualTo("Platform");
		assertThat(found.getStatus()).isEqualTo(TaskStatus.TODO);
		assertThat(found.getCreatedAt()).isNotNull();
	}

}
