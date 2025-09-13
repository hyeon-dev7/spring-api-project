package com.example.bookstore.restcontroller;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.entity.User;
import com.example.bookstore.service.MapperService;
import com.example.bookstore.service.ReviewService;
import com.example.bookstore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final MapperService mapperService;
    private final UserService userService;

    public ReviewController(ReviewService reviewService, MapperService mapperService, UserService userService) {
        this.reviewService = reviewService;
        this.mapperService = mapperService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody @Valid ReviewDTO reviewDTO,
                                    BindingResult bindingResult, HttpServletRequest request) {
        // 매개변수로 @ModelAttribute UserDTO currentUser 받아서 바로 사용할 수도 있지만 jwt를 이용해서 인증하고 싶었음
        Optional<User> user = userService.getCurrentUser(request);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 후 작성 가능합니다"));
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(mapperService.mapValidationErrors(bindingResult));
        }
        try {
            reviewService.createReview(reviewDTO, user.get().getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("리뷰 저장 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "처리 중 오류가 발생했습니다."));
        }
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<?> delete(@PathVariable("reviewId") Long reviewId,
                                    HttpServletRequest request) {
        Optional<User> user = userService.getCurrentUser(request);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "로그인 후 삭제 가능합니다."));
        }
        boolean del = reviewService.deleteReview(reviewId, user.get().getId());
        if (del) return ResponseEntity.ok().build();
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "삭제 권한이 없거나 이미 삭제된 후기입니다."));
        }
    }

    @PutMapping("/update/{reviewId}")
    public ResponseEntity<?> update(@RequestBody @Valid ReviewDTO reviewDTO, BindingResult bindingResult,
                                    @PathVariable Long reviewId, HttpServletRequest request) {
        Optional<User> user = userService.getCurrentUser(request);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","로그인 후 수정 가능합니다."));
        }
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(mapperService.mapValidationErrors(bindingResult));
        }
        boolean updated = reviewService.updateReview(reviewDTO, reviewId, user.get().getId());
        if (updated) return ResponseEntity.ok().build();
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message","수정 권한이 없습니다."));
        }
    }
}