package com.springboot.controller;

import com.springboot.dto.SignupRequest;
import com.springboot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @GetMapping("/login")
  public String loginPage(@RequestParam(value = "error", required = false) String error,
                          @RequestParam(value = "logout", required = false) String logout,
                          Model model) {
    if (error != null)  model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
    if (logout != null) model.addAttribute("logout", "로그아웃 되었습니다.");
    model.addAttribute("title", "로그인");
    return "auth/login";
  }

  // ✅ 회원가입
  @GetMapping("/signup")
  public String signupForm(Model model) {
    model.addAttribute("form", new SignupRequest("", "", "", ""));
    model.addAttribute("title", "회원가입");
    return "auth/signup";
  }

  @PostMapping("/signup")
  public String signup(@Valid @ModelAttribute("form") SignupRequest form,
                       BindingResult binding, Model model) {
    // 비밀번호 일치 검증
    if (!form.password().equals(form.confirmPassword())) {
      binding.rejectValue("confirmPassword", "mismatch", "비밀번호가 일치하지 않습니다.");
    }
    if (binding.hasErrors()) {
      model.addAttribute("title", "회원가입");
      return "auth/signup";
    }

    try {
      authService.signup(form);
    } catch (IllegalArgumentException e) {
      // 중복 아이디 등
      binding.rejectValue("username", "duplicate", e.getMessage());
      model.addAttribute("title", "회원가입");
      return "auth/signup";
    }

    // 가입 성공 → 로그인 페이지로
    return "redirect:/login?signup";
  }
}
