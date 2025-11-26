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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

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

    model.addAttribute("page", result);
    model.addAttribute("category", category);
    model.addAttribute("q", q);
    model.addAttribute("sort", sort);
    return "board/list";
  }

  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id,
                       Authentication authentication,
                       Model model) {

      var post = boardService.get(id, true);

      boolean isAdmin = authentication != null &&
          authentication.getAuthorities().stream()
              .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

      // 숨김 글 + 관리자 아닌 경우 404
      if (post.hidden() && !isAdmin) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }

      model.addAttribute("post", post);
      model.addAttribute("commentForm", new CommentCreateRequest(""));
      return "board/detail";
  }

  @GetMapping("/new")
  public String createForm(Model model) {
      model.addAttribute("mode", "create");
      model.addAttribute("form", new PostCreateRequest("", "", null));
      return "board/form";
  }

  @PostMapping("/new")
  public String create(
          @Valid @ModelAttribute("form") PostCreateRequest form,
          BindingResult bindingResult,
          @AuthenticationPrincipal UserDetails userDetails,
          @RequestParam(value = "attach", required = false) MultipartFile file
  ) {
      if (bindingResult.hasErrors()) {
          return "board/form";
      }
      String username = userDetails.getUsername();
      Long id = boardService.create(username, form, file);
      return "redirect:/board/" + id;
  }

  @PostMapping("/{id}/edit")
  public String update(
          @PathVariable Long id,
          @Valid @ModelAttribute("form") PostUpdateRequest form,
          BindingResult bindingResult,
          @AuthenticationPrincipal UserDetails userDetails,
          @RequestParam(value = "attach", required = false) MultipartFile file
  ) {
      if (bindingResult.hasErrors()) {
          return "board/form";
      }
      String username = userDetails.getUsername();
      boardService.update(id, username, form, file);
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
