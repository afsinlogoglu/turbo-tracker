package com.turbo.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.request.TaskRequest;
import com.turbo.tracker.dto.request.TaskUpdateRequest;
import com.turbo.tracker.dto.response.TaskResponse;
import com.turbo.tracker.entity.Project;
import com.turbo.tracker.entity.Task;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.BadRequestException;
import com.turbo.tracker.exception.NotFoundException;
import com.turbo.tracker.repository.CommentRepository;
import com.turbo.tracker.repository.ProjectMemberRepository;
import com.turbo.tracker.repository.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository tasks;
	private final ProjectService projects;
	private final UserService users;
	private final ProjectMemberRepository members;
	private final CommentRepository comments;
	private final CurrentUser current;

	public TaskService(
			TaskRepository tasks,
			ProjectService projects,
			UserService users,
			ProjectMemberRepository members,
			CommentRepository comments,
			CurrentUser current) {
		this.tasks = tasks;
		this.projects = projects;
		this.users = users;
		this.members = members;
		this.comments = comments;
		this.current = current;
	}

	@Transactional(readOnly = true)
	public List<TaskResponse> list(Long projectId) {
		current.requireMember(projectId);
		projects.find(projectId);
		return tasks.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream().map(TaskResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public TaskResponse get(Long id) {
		return TaskResponse.from(find(id));
	}

	@Transactional
	public TaskResponse create(Long projectId, TaskRequest request) {
		current.requireMember(projectId);
		Project project = projects.find(projectId);
		Task task = tasks.save(new Task(project, request.title().trim(), request.description(), request.priority()));
		if (request.assigneeId() != null) {
			applyAssignee(task, request.assigneeId());
		}
		return TaskResponse.from(task);
	}

	@Transactional
	public TaskResponse update(Long id, TaskUpdateRequest request) {
		Task task = find(id);
		task.changeStatus(request.status());
		task.changePriority(request.priority());
		if (request.assigneeTouched()) {
			applyAssignee(task, request.assigneeId());
		}
		return TaskResponse.from(task);
	}

	@Transactional
	public void delete(Long id) {
		Task task = find(id);
		comments.deleteByTaskId(id);
		tasks.delete(task);
	}

	public Task find(Long id) {
		Task task = tasks.findById(id).orElseThrow(() -> new NotFoundException("Görev bulunamadı: " + id));
		current.requireMember(task.getProject().getId());
		return task;
	}

	private void applyAssignee(Task task, Long assigneeId) {
		if (assigneeId == null) {
			task.assign(null);
			return;
		}
		User user = users.find(assigneeId);
		if (!members.existsByProjectIdAndUserId(task.getProject().getId(), user.getId())) {
			throw new BadRequestException("Bu kullanıcı projenin üyesi değil");
		}
		task.assign(user);
	}

}
