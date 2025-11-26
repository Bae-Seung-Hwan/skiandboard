package com.springboot.controller;

import com.springboot.dto.AdminUserEditRequest;
import com.springboot.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    // 관리자 대시보드
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("userCount", adminService.countUsers());
        model.addAttribute("resortCount", adminService.countResorts());
        model.addAttribute("postCount", adminService.countPosts());
        model.addAttribute("commentCount", adminService.countComments());

        model.addAttribute("recentUsers", adminService.getRecentUsers(5));
        model.addAttribute("recentPosts", adminService.getRecentPosts(5));

        return "admin/index";
    }

    // 사용자 목록
    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    
    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable("id") Long id, Model model) {
        var form = adminService.getUserForEdit(id);
        model.addAttribute("userId", id);
        model.addAttribute("form", form);
        model.addAttribute("roles", com.springboot.domain.Role.values());
        return "admin/user-edit";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("form") @Valid AdminUserEditRequest form,
                             BindingResult binding,
                             Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("roles", com.springboot.domain.Role.values());
            return "admin/user-edit";
        }

        adminService.updateUser(id, form);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable("id") Long id) {
        adminService.toggleUserEnabled(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return "redirect:/admin/users";
    }
    //게시판 관리
    @GetMapping("/posts")
    public String adminPosts(Model model) {
        model.addAttribute("posts", adminService.getAllPostsForAdmin());
        return "admin/posts";
    }

    @PostMapping("/posts/{id}/toggle-hidden")
    public String togglePostHidden(@PathVariable("id") Long id) {
        adminService.togglePostHidden(id);
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable("id") Long id) {
        adminService.deletePost(id);
        return "redirect:/admin/posts";
    }
    // 스키장 목록
    @GetMapping("/resorts")
    public String resorts(Model model) {
        model.addAttribute("resorts", adminService.getAllResorts());
        return "admin/resorts";
    }

}
