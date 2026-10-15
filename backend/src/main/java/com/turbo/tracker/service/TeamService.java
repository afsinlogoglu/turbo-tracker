package com.turbo.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.request.TeamRequest;
import com.turbo.tracker.dto.request.TeamUpdateRequest;
import com.turbo.tracker.dto.response.TeamResponse;
import com.turbo.tracker.entity.Team;
import com.turbo.tracker.exception.BadRequestException;
import com.turbo.tracker.exception.ConflictException;
import com.turbo.tracker.exception.NotFoundException;
import com.turbo.tracker.repository.ProjectRepository;
import com.turbo.tracker.repository.TeamRepository;

@Service
public class TeamService {

	private final TeamRepository teams;
	private final ProjectRepository projects;
	private final CurrentUser current;

	public TeamService(TeamRepository teams, ProjectRepository projects, CurrentUser current) {
		this.teams = teams;
		this.projects = projects;
		this.current = current;
	}

	@Transactional(readOnly = true)
	public List<TeamResponse> list() {
		current.require();
		return teams.findAllByOrderByCreatedAtDesc().stream()
				.map(team -> TeamResponse.from(team, projects.countByTeamId(team.getId())))
				.toList();
	}

	@Transactional(readOnly = true)
	public TeamResponse get(Long id) {
		current.require();
		Team team = find(id);
		return TeamResponse.from(team, projects.countByTeamId(id));
	}

	@Transactional
	public TeamResponse create(TeamRequest request) {
		current.require();
		String name = request.name().trim();
		if (teams.existsByNameIgnoreCase(name)) {
			throw new ConflictException("Bu takım adı zaten var");
		}
		Team team = teams.save(new Team(name, request.description()));
		return TeamResponse.from(team, 0);
	}

	@Transactional
	public TeamResponse update(Long id, TeamUpdateRequest request) {
		current.require();
		Team team = find(id);
		if (request.name() != null) {
			String name = request.name().trim();
			if (name.isBlank()) {
				throw new BadRequestException("İsim boş olamaz");
			}
			if (teams.existsByNameIgnoreCaseAndIdNot(name, id)) {
				throw new ConflictException("Bu takım adı zaten var");
			}
			team.rename(name);
		}
		if (request.description() != null) {
			team.changeDescription(request.description());
		}
		return TeamResponse.from(team, projects.countByTeamId(id));
	}

	@Transactional
	public void delete(Long id) {
		current.require();
		Team team = find(id);
		if (projects.existsByTeamId(id)) {
			throw new ConflictException("Bu takımın hâlâ projesi var");
		}
		teams.delete(team);
	}

	public Team find(Long id) {
		return teams.findById(id).orElseThrow(() -> new NotFoundException("Takım bulunamadı: " + id));
	}

}
