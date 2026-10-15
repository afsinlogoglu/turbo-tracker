package com.turbo.tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turbo.tracker.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	List<Project> findAllByTeamIdOrderByCreatedAtDesc(Long teamId);

	boolean existsByTeamId(Long teamId);

	boolean existsByTeamIdAndNameIgnoreCase(Long teamId, String name);

	boolean existsByTeamIdAndNameIgnoreCaseAndIdNot(Long teamId, String name, Long id);

	long countByTeamId(Long teamId);

}
