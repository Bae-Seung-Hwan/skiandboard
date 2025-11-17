package com.springboot.service;

import com.springboot.dto.SignupRequest;

public interface AuthService {
  void signup(SignupRequest req);
}
