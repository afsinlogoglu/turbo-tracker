package com.turbo.tracker.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.response.UserResponse;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.NotFoundException;
import com.turbo.tracker.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository users;

	public UserService(UserRepository users) {
		this.users = users;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> list() {
		return users.findAllByOrderByNameAsc().stream().map(UserResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public User find(Long id) {
		return users.findById(id).orElseThrow(() -> new NotFoundException("Kullanıcı bulunamadı: " + id));
	}

}
