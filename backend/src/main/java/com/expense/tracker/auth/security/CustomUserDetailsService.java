package com.expense.tracker.auth.security;

import com.expense.tracker.user.entity.User;
import com.expense.tracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CONCEPT: UserDetailsService (a Spring Security interface)
 * Spring Security doesn't know anything about our `User` JPA entity - it
 * only understands its own `UserDetails` contract (username, password,
 * authorities/roles, account-enabled flags). This class is the ADAPTER
 * between our domain model and Spring Security's world.
 *
 * CONCEPT: Interfaces
 * `UserDetailsService` is an interface with one method: loadUserByUsername.
 * By implementing it, we plug our own logic (look up by email in MySQL)
 * into Spring Security's authentication process without Spring Security
 * needing to know HOW users are stored.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // org.springframework.security.core.userdetails.User is Spring's built-in
        // UserDetails implementation - we adapt our entity into it.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
