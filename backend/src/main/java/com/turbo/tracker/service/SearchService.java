package com.turbo.tracker.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.response.PageResponse;
import com.turbo.tracker.dto.response.TaskResponse;
import com.turbo.tracker.entity.Role;
import com.turbo.tracker.entity.TaskPriority;
import com.turbo.tracker.entity.TaskStatus;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.BadRequestException;
import com.turbo.tracker.repository.ProjectMemberRepository;
import com.turbo.tracker.repository.TaskSearch;

@Service
public class SearchService {

	private static final Set<String> SORTS = Set.of("newest", "title", "priority");

	private final TaskSearch search;
	private final ProjectService projects;
	private final CurrentUser current;
	private final ProjectMemberRepository members;

	public SearchService(TaskSearch search, ProjectService projects, CurrentUser current, ProjectMemberRepository members) {
		this.search = search;
		this.projects = projects;
		this.current = current;
		this.members = members;
	}

	@Transactional(readOnly = true)
	public PageResponse<TaskResponse> tasks(
			Long projectId,
			String query,
			TaskStatus status,
			TaskPriority priority,
			Long assigneeId,
			boolean unassigned,
			String sort,
			int page,
			int size) {
		if (!SORTS.contains(sort)) {
			throw new BadRequestException("sort is invalid");
		}
		int safePage = Math.max(page, 0);
		int safeSize = size <= 0 ? 20 : Math.min(size, 100);
		User user = current.require();
		List<Long> projectIds = null;
		if (projectId != null) {
			projects.find(projectId);
			current.requireMember(projectId);
		}
		else if (user.getRole() != Role.ADMIN) {
			projectIds = members.findProjectIdsByUserId(user.getId());
		}
		TaskSearch.Criteria criteria = new TaskSearch.Criteria(projectId, projectIds, query, status, priority, assigneeId, unassigned, sort);
		long total = search.count(criteria);
		List<TaskResponse> content = search.find(criteria, safePage, safeSize).stream().map(TaskResponse::from).toList();
		return PageResponse.of(content, safePage, safeSize, total);
	}

}
