package com.turbo.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.request.CommentRequest;
import com.turbo.tracker.dto.response.CommentResponse;
import com.turbo.tracker.entity.Comment;
import com.turbo.tracker.entity.Role;
import com.turbo.tracker.entity.Task;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.ForbiddenException;
import com.turbo.tracker.exception.NotFoundException;
import com.turbo.tracker.repository.CommentRepository;

@Service
public class CommentService {

	private final CommentRepository comments;
	private final TaskService tasks;
	private final CurrentUser current;

	public CommentService(CommentRepository comments, TaskService tasks, CurrentUser current) {
		this.comments = comments;
		this.tasks = tasks;
		this.current = current;
	}

	@Transactional(readOnly = true)
	public List<CommentResponse> list(Long taskId) {
		Task task = tasks.find(taskId);
		current.requireMember(task.getProject().getId());
		return comments.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream().map(CommentResponse::from).toList();
	}

	@Transactional
	public CommentResponse create(Long taskId, CommentRequest request) {
		Task task = tasks.find(taskId);
		current.requireMember(task.getProject().getId());
		Comment comment = comments.save(new Comment(task, current.require(), request.body().trim()));
		return CommentResponse.from(comment);
	}

	@Transactional
	public void delete(Long id) {
		Comment comment = comments.findById(id).orElseThrow(() -> new NotFoundException("Yorum bulunamadı: " + id));
		User user = current.require();
		current.requireMember(comment.getTask().getProject().getId());
		if (user.getRole() != Role.ADMIN && !comment.getAuthor().getId().equals(user.getId())) {
			throw new ForbiddenException("Bu yorumu sadece yazan silebilir");
		}
		comments.delete(comment);
	}

}
