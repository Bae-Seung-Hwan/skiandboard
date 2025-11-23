package com.springboot.controller;

import com.springboot.dto.ProfileUpdateRequest;
import com.springboot.security.UserPrincipal;
import com.springboot.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageController {

  private final MyPageService myPageService;

  // ✅ 마이페이지 요약
  @GetMapping
  public String index(@AuthenticationPrincipal UserPrincipal principal,
                      Model model) {
    String username = principal.getUsername();
    String displayName = principal.getDisplayName();

    model.addAttribute("displayName", displayName);
    model.addAttribute("username", username);
    model.addAttribute("recentPosts", myPageService.getRecentPosts(username));
    model.addAttribute("recentComments", myPageService.getRecentComments(username));

    return "mypage/index";
  }

  // ✅ 내 정보 보기 + 수정 폼
  @GetMapping("/profile")
  public String profile(@AuthenticationPrincipal UserPrincipal principal,
                        Model model) {
    String username = principal.getUsername();
    var form = myPageService.getProfile(username);
    model.addAttribute("form", form);
    model.addAttribute("username", username);
    return "mypage/profile";
  }

  @PostMapping("/profile")
  public String updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                              @Valid @ModelAttribute("form") ProfileUpdateRequest form,
                              BindingResult binding,
                              Model model) {
    String username = principal.getUsername();
    if (binding.hasErrors()) {
      model.addAttribute("username", username);
      return "mypage/profile";
    }
    myPageService.updateProfile(username, form);
    return "redirect:/mypage?updated";
  }

  // ✅ 내가 쓴 글 전체
  @GetMapping("/posts")
  public String myPosts(@AuthenticationPrincipal UserPrincipal principal,
                        Model model) {
    String username = principal.getUsername();
    model.addAttribute("posts", myPageService.getAllPosts(username));
    model.addAttribute("displayName", principal.getDisplayName());
    return "mypage/posts";
  }

  // ✅ 내가 쓴 댓글 전체
  @GetMapping("/comments")
  public String myComments(@AuthenticationPrincipal UserPrincipal principal,
                           Model model) {
    String username = principal.getUsername();
    model.addAttribute("comments", myPageService.getAllComments(username));
    model.addAttribute("displayName", principal.getDisplayName());
    return "mypage/comments";
  }
}
