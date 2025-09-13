package com.example.bookstore.controller;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.jwt.JwtAuthenticationFilter;
import com.example.bookstore.service.BookDetailService;
import com.example.bookstore.service.ReviewService;
import com.example.bookstore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;


@Controller
public class UserController {

    private final UserService userService;
    private final ReviewService reviewService;
    private final BookRepository bookRepository;
    private final BookDetailService bookDetailService;

    public UserController(UserService userService, ReviewService reviewService, BookDetailService bookDetailService, BookRepository bookRepository) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.bookDetailService = bookDetailService;
        this.bookRepository = bookRepository;
    }

    @GetMapping("/login")
    public String login(){
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(){
        return "auth/register";
    }


    @GetMapping("/mypage")
    public String mypage(@RequestParam(defaultValue = "0") int page,
                         HttpServletRequest request, Model model) {
        Optional<User> optionalUser = userService.getCurrentUser(request);
        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }
        Pageable pageable = PageRequest.of(page, 20);
        Page<ReviewDTO> reviews = reviewService.getReviewsByUserId(optionalUser.get().getId(), pageable);
        if (reviews.isEmpty() && page>0) {
            return "redirect:/mypage?page=" + (page-1);
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewed", reviews.getTotalElements()>0);
        return "auth/mypage";
    }
}
