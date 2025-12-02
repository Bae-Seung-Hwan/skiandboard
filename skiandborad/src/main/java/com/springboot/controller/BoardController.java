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
  private final BoardService boardService; //게시판 관련 비지니스 로직 담당하는 서비스 계층

  @GetMapping
  public String list( //게시글 목록 조회
      @RequestParam(name = "category", required = false) BoardCategory category,
      @RequestParam(name = "q",        required = false) String q,
      @RequestParam(name = "page",     required = false, defaultValue = "0") int page,
      @RequestParam(name = "sort",     required = false, defaultValue = "createdAt,desc") String sort,
      Model model) {
	  //조회수 정렬인지 판단
    Sort s = "views".equalsIgnoreCase(sort) 
        ? Sort.by(Sort.Direction.DESC, "viewCount")
        : Sort.by(Sort.Direction.DESC, "createdAt");

    Pageable pageable = PageRequest.of(page, 10, s);
    //서비스에서 DTO 형태로 목록 조회
    Page<PostListItemDto> result = boardService.list(category, q, pageable);
    // 템플릿 렌더링용 데이터
    model.addAttribute("page", result);
    model.addAttribute("category", category);
    model.addAttribute("q", q);
    model.addAttribute("sort", sort);
    return "board/list";
  }
//게시글 상세 페이지
  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id,
                       Authentication authentication,
                       Model model) {
	  //게시글 + 댓글 목록 포함된 DTO
      var post = boardService.get(id, true);
      //관리자인지 확인
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
  // 게시글 작성 폼
  @GetMapping("/new")
  public String createForm(Model model) {
      model.addAttribute("mode", "create");
      model.addAttribute("form", new PostCreateRequest("", "", null));
      return "board/form";
  }
  //게시글 작성 처리
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
      Long id = boardService.create(username, form, file); //글 생석 + 파일 업로드
      return "redirect:/board/" + id;
  }
  //게시글 수정
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

  //게시글 삭제
  @PostMapping("/{id}/delete")
  public String delete(@PathVariable("id") Long id,
                       @AuthenticationPrincipal UserDetails principal) {
    boardService.delete(id, principal.getUsername());
    return "redirect:/board";
  }
  //댓글 작성
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
