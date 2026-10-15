package com.turbo.tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turbo.tracker.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

	boolean existsByProjectId(Long projectId);

	long countByProjectId(Long projectId);

}
