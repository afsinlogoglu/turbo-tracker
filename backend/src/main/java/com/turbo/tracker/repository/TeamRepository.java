package com.turbo.tracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turbo.tracker.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

	List<Team> findAllByOrderByCreatedAtDesc();

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

}
