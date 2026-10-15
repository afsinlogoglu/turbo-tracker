package com.turbo.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.request.MemberRequest;
import com.turbo.tracker.dto.response.MemberResponse;
import com.turbo.tracker.entity.Project;
import com.turbo.tracker.entity.ProjectMember;
import com.turbo.tracker.entity.ProjectRole;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.ConflictException;
import com.turbo.tracker.exception.NotFoundException;
import com.turbo.tracker.repository.ProjectMemberRepository;

@Service
public class ProjectMemberService {

	private final ProjectMemberRepository members;
	private final ProjectService projects;
	private final UserService users;
	private final CurrentUser current;

	public ProjectMemberService(ProjectMemberRepository members, ProjectService projects, UserService users, CurrentUser current) {
		this.members = members;
		this.projects = projects;
		this.users = users;
		this.current = current;
	}

	@Transactional(readOnly = true)
	public List<MemberResponse> list(Long projectId) {
		current.requireMember(projectId);
		projects.find(projectId);
		return members.findAllByProjectIdOrderByIdAsc(projectId).stream().map(MemberResponse::from).toList();
	}

	@Transactional
	public MemberResponse add(Long projectId, MemberRequest request) {
		current.requireOwner(projectId);
		Project project = projects.find(projectId);
		if (members.existsByProjectIdAndUserId(projectId, request.userId())) {
			throw new ConflictException("Bu kullanıcı zaten projede");
		}
		User user = users.find(request.userId());
		ProjectRole role = request.role() == null ? ProjectRole.MEMBER : request.role();
		ProjectMember member = members.save(new ProjectMember(project, user, role));
		return MemberResponse.from(member);
	}

	@Transactional
	public void remove(Long projectId, Long userId) {
		current.requireOwner(projectId);
		projects.find(projectId);
		ProjectMember member = members.findByProjectIdAndUserId(projectId, userId)
				.orElseThrow(() -> new NotFoundException("Üye bulunamadı: " + userId));
		if (member.getRole() == ProjectRole.OWNER && members.countByProjectIdAndRole(projectId, ProjectRole.OWNER) == 1) {
			throw new ConflictException("Projenin son sahibi çıkarılamaz");
		}
		members.delete(member);
	}

}
