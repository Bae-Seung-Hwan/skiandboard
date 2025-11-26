package com.springboot.service;

import com.springboot.domain.Role;
import com.springboot.domain.User;
import com.springboot.dto.SignupRequest;
import com.springboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void signup(SignupRequest req) {
    // 사용자명 중복
    userRepository.findByUsername(req.username()).ifPresent(u -> {
      throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
    });

    // 비밀번호 일치 확인
    if (!req.password().equals(req.confirmPassword())) {
      throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
    }

    var user = User.builder()
    	    .username(req.username())
    	    .password(passwordEncoder.encode(req.password()))
    	    .displayName(req.displayName())
    	    .role(Role.USER)
    	    .enabled(true)
    	    .build();

    userRepository.save(user);
  }
}
