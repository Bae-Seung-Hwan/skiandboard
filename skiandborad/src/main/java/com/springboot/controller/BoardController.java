package com.springboot.controller;

import com.springboot.domain.BoardCategory;
import com.springboot.dto.*;
import com.springboot.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {
  private final BoardService boardService;

  @GetMapping
  public String list(
      @RequestParam(name = "category", required = false) BoardCategory category,
      @RequestParam(name = "q",        required = false) String q,
      @RequestParam(name = "page",     required = false, defaultValue = "0") int page,
      @RequestParam(name = "sort",     required = false, defaultValue = "createdAt,desc") String sort,
      Model model) {

    Sort s = "views".equalsIgnoreCase(sort)
        ? Sort.by(Sort.Direction.DESC, "viewCount")
        : Sort.by(Sort.Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page, 10, s);
    Page<PostListItemDto> result = boardService.list(category, q, pageable);

    model.addAttribute("posts", result);
    model.addAttribute("category", category);
    model.addAttribute("q", q);
    model.addAttribute("sort", sort);
    return "board/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id, Model model) {
    model.addAttribute("post", boardService.get(id, true));
    model.addAttribute("commentForm", new CommentCreateRequest(""));
    return "board/detail";
  }

  @GetMapping("/new")
  public String newForm(Model model) {
    // 하드코딩 버전(정확한 상수명 사용)
    // model.addAttribute("form", new PostCreateRequest("", "", BoardCategory.FREE));

    var first = BoardCategory.values()[0];
    model.addAttribute("form", new PostCreateRequest("", "", first));

    model.addAttribute("mode", "create");
    return "board/form";
  }

  @PostMapping("/new")
  public String create(@AuthenticationPrincipal UserDetails principal,
                       @Valid @ModelAttribute("form") PostCreateRequest form,
                       BindingResult binding) {
    if (binding.hasErrors()) return "board/form";
    Long id = boardService.create(principal.getUsername(), form);
    return "redirect:/board/" + id;
  }

  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable("id") Long id, Model model) {
    var post = boardService.get(id, false);
    model.addAttribute("form", new PostUpdateRequest(post.title(), post.content(), post.category()));
    model.addAttribute("mode", "edit");
    model.addAttribute("postId", id);
    return "board/form";
  }

  @PostMapping("/{id}/edit")
  public String update(@PathVariable("id") Long id,
                       @AuthenticationPrincipal UserDetails principal,
                       @Valid @ModelAttribute("form") PostUpdateRequest form,
                       BindingResult binding) {
    if (binding.hasErrors()) return "board/form";
    boardService.update(id, principal.getUsername(), form);
    return "redirect:/board/" + id;
  }

  @PostMapping("/{id}/delete")
  public String delete(@PathVariable("id") Long id,
                       @AuthenticationPrincipal UserDetails principal) {
    boardService.delete(id, principal.getUsername());
    return "redirect:/board";
  }

  @PostMapping("/{id}/comments")
  public String addComment(@PathVariable("id") Long id,
                           @AuthenticationPrincipal UserDetails principal,
                           @Valid @ModelAttribute("commentForm") CommentCreateRequest form,
                           BindingResult binding) {
    if (!binding.hasErrors()) {
      String username = principal != null ? principal.getUsername() : null;
      boardService.addComment(id, username, form);
    }
    return "redirect:/board/" + id;
  }
}
