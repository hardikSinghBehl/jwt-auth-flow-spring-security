package com.behl.cerberus.security;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.behl.cerberus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(final String userId) {
		return convert(userRepository.findById(UUID.fromString(userId))
				.orElseThrow(() -> new UsernameNotFoundException("Bad Credentials")));
	}

	private User convert(com.behl.cerberus.entity.User user) {
		return new User(user.getEmailId(), user.getPassword(), List.of());
	}

}