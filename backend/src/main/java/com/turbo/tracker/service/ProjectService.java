package com.turbo.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.request.ProjectRequest;
import com.turbo.tracker.dto.request.ProjectUpdateRequest;
import com.turbo.tracker.dto.response.ProjectResponse;
import com.turbo.tracker.entity.Project;
import com.turbo.tracker.entity.ProjectMember;
import com.turbo.tracker.entity.ProjectRole;
import com.turbo.tracker.entity.Team;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.BadRequestException;
import com.turbo.tracker.exception.ConflictException;
import com.turbo.tracker.exception.NotFoundException;
import com.turbo.tracker.repository.ProjectMemberRepository;
import com.turbo.tracker.repository.ProjectRepository;
import com.turbo.tracker.repository.TaskRepository;

@Service
public class ProjectService {

	private final ProjectRepository projects;
	private final TaskRepository tasks;
	private final TeamService teams;
	private final ProjectMemberRepository members;
	private final CurrentUser current;

	public ProjectService(
			ProjectRepository projects,
			TaskRepository tasks,
			TeamService teams,
			ProjectMemberRepository members,
			CurrentUser current) {
		this.projects = projects;
		this.tasks = tasks;
		this.teams = teams;
		this.members = members;
		this.current = current;
	}

	@Transactional(readOnly = true)
	public List<ProjectResponse> list(Long teamId) {
		current.require();
		teams.find(teamId);
		return projects.findAllByTeamIdOrderByCreatedAtDesc(teamId).stream()
				.map(project -> ProjectResponse.from(project, tasks.countByProjectId(project.getId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public ProjectResponse get(Long id) {
		current.require();
		Project project = find(id);
		return ProjectResponse.from(project, tasks.countByProjectId(id));
	}

	@Transactional
	public ProjectResponse create(Long teamId, ProjectRequest request) {
		Team team = teams.find(teamId);
		String name = request.name().trim();
		if (projects.existsByTeamIdAndNameIgnoreCase(teamId, name)) {
			throw new ConflictException("Bu takımda bu proje adı zaten var");
		}
		User owner = current.require();
		Project project = projects.save(new Project(team, name, request.description()));
		members.save(new ProjectMember(project, owner, ProjectRole.OWNER));
		return ProjectResponse.from(project, 0);
	}

	@Transactional
	public ProjectResponse update(Long id, ProjectUpdateRequest request) {
		current.requireMember(id);
		Project project = find(id);
		if (request.name() != null) {
			String name = request.name().trim();
			if (name.isBlank()) {
				throw new BadRequestException("İsim boş olamaz");
			}
			if (projects.existsByTeamIdAndNameIgnoreCaseAndIdNot(project.getTeam().getId(), name, id)) {
				throw new ConflictException("Bu takımda bu proje adı zaten var");
			}
			project.rename(name);
		}
		if (request.description() != null) {
			project.changeDescription(request.description());
		}
		return ProjectResponse.from(project, tasks.countByProjectId(id));
	}

	@Transactional
	public void delete(Long id) {
		current.requireOwner(id);
		Project project = find(id);
		if (tasks.existsByProjectId(id)) {
			throw new ConflictException("Bu projede hâlâ görev var");
		}
		members.deleteByProjectId(id);
		projects.delete(project);
	}

	public Project find(Long id) {
		return projects.findById(id).orElseThrow(() -> new NotFoundException("Proje bulunamadı: " + id));
	}

}
