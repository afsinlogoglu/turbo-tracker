package com.turbo.tracker.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.turbo.tracker.repository.UserRepository;

@Service
public class TurboUserDetailsService implements UserDetailsService {

	private final UserRepository users;

	public TurboUserDetailsService(UserRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		return users.findByEmailIgnoreCase(email)
				.map(AuthUser::new)
				.orElseThrow(() -> new UsernameNotFoundException(email));
	}

}
