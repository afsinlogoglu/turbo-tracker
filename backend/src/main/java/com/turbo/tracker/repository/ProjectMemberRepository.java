package com.turbo.tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.turbo.tracker.entity.ProjectMember;
import com.turbo.tracker.entity.ProjectRole;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

	List<ProjectMember> findAllByProjectIdOrderByIdAsc(Long projectId);

	boolean existsByProjectIdAndUserId(Long projectId, Long userId);

	Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

	void deleteByProjectId(Long projectId);

	long countByProjectIdAndRole(Long projectId, ProjectRole role);

	@Query("select member.project.id from ProjectMember member where member.user.id = :userId")
	List<Long> findProjectIdsByUserId(Long userId);

}
