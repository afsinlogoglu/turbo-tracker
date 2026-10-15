package com.turbo.tracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turbo.tracker.dto.request.LoginRequest;
import com.turbo.tracker.dto.request.RegisterRequest;
import com.turbo.tracker.dto.response.AuthResponse;
import com.turbo.tracker.dto.response.UserResponse;
import com.turbo.tracker.entity.Role;
import com.turbo.tracker.entity.User;
import com.turbo.tracker.exception.ConflictException;
import com.turbo.tracker.exception.UnauthorizedException;
import com.turbo.tracker.repository.UserRepository;
import com.turbo.tracker.security.JwtService;

@Service
public class AuthService {

	private final UserRepository users;
	private final PasswordEncoder passwords;
	private final JwtService jwt;
	private final CurrentUser current;

	public AuthService(UserRepository users, PasswordEncoder passwords, JwtService jwt, CurrentUser current) {
		this.users = users;
		this.passwords = passwords;
		this.jwt = jwt;
		this.current = current;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String email = request.email().trim().toLowerCase();
		if (users.existsByEmailIgnoreCase(email)) {
			throw new ConflictException("Bu e-posta zaten kayıtlı");
		}
		Role role = users.count() == 0 ? Role.ADMIN : Role.MEMBER;
		User user = users.save(new User(request.name().trim(), email, passwords.encode(request.password()), role));
		return new AuthResponse(jwt.issue(user), UserResponse.from(user));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = users.findByEmailIgnoreCase(request.email().trim())
				.filter(found -> passwords.matches(request.password(), found.getPasswordHash()))
				.orElseThrow(() -> new UnauthorizedException("E-posta veya şifre hatalı"));
		return new AuthResponse(jwt.issue(user), UserResponse.from(user));
	}

	@Transactional(readOnly = true)
	public UserResponse me() {
		return UserResponse.from(current.require());
	}

}
