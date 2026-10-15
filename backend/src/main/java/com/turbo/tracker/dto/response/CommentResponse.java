package com.turbo.tracker.dto.response;

import java.time.Instant;

import com.turbo.tracker.entity.Comment;

public record CommentResponse(
		Long id,
		Long taskId,
		Long authorId,
		String authorName,
		String body,
		Instant createdAt) {

	public static CommentResponse from(Comment comment) {
		return new CommentResponse(
				comment.getId(),
				comment.getTask().getId(),
				comment.getAuthor().getId(),
				comment.getAuthor().getName(),
				comment.getBody(),
				comment.getCreatedAt());
	}

}
