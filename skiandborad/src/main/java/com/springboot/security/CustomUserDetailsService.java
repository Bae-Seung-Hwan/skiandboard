package com.springboot.security;

import com.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var u = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));

    return new UserPrincipal(
        u.getId(),
        u.getUsername(),
        u.getPassword(),       // BCrypt 해시
        u.getDisplayName(),    // 닉네임 주입
        u.isEnabled(),
        authorities
    );
  }
}
