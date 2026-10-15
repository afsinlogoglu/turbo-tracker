package com.turbo.tracker.dto.response;

import com.turbo.tracker.entity.Role;
import com.turbo.tracker.entity.User;

public record UserResponse(Long id, String name, String email, Role role) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
	}

}
