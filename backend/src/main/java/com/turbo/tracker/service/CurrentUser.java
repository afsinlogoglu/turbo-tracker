package com.turbo.tracker.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.turbo.tracker.entity.ProjectRole;
import com.turbo.tracker.entity.Role;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.ForbiddenException;
import com.turbo.tracker.exception.UnauthorizedException;
import com.turbo.tracker.repository.ProjectMemberRepository;
import com.turbo.tracker.repository.UserRepository;

@Component
public class CurrentUser {

	private final UserRepository users;
	private final ProjectMemberRepository members;

	public CurrentUser(UserRepository users, ProjectMemberRepository members) {
		this.users = users;
		this.members = members;
	}

	public User require() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() instanceof String) {
			throw new UnauthorizedException("Giriş gerekli");
		}
		return users.findByEmailIgnoreCase(authentication.getName())
				.orElseThrow(() -> new UnauthorizedException("Giriş gerekli"));
	}

	public void requireMember(Long projectId) {
		User user = require();
		if (user.getRole() == Role.ADMIN) {
			return;
		}
		if (!members.existsByProjectIdAndUserId(projectId, user.getId())) {
			throw new ForbiddenException("Bu projenin üyesi değilsin");
		}
	}

	public void requireOwner(Long projectId) {
		User user = require();
		if (user.getRole() == Role.ADMIN) {
			return;
		}
		boolean owner = members.findByProjectIdAndUserId(projectId, user.getId())
				.map(member -> member.getRole() == ProjectRole.OWNER)
				.orElse(false);
		if (!owner) {
			throw new ForbiddenException("Bu işlem proje sahibine ait");
		}
	}

}
