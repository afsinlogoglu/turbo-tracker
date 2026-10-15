package com.turbo.tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turbo.tracker.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findAllByTaskIdOrderByCreatedAtAsc(Long taskId);

	void deleteByTaskId(Long taskId);

}
