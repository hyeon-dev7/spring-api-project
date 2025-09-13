package com.example.bookstore.controller;


import com.example.bookstore.dto.BookDetailDTO;
import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.BookDetailService;
import com.example.bookstore.service.CacheService;
import com.example.bookstore.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class BookController {

    private final CacheService cacheService;
    private final ReviewService reviewService;
    private final BookDetailService bookDetailService;

    public BookController(CacheService cacheService, ReviewService reviewService, BookDetailService bookDetailService) {
        this.cacheService = cacheService;
        this.reviewService = reviewService;
        this.bookDetailService = bookDetailService;
    }

    @GetMapping("")
    public String index() {
        return "home";
    }

    @GetMapping("/book/popular")
    public String books() {
        return "book/popular";
    }


    @GetMapping("/book/search")
    public String review(@RequestParam(required = false) String keyword,
                         @RequestParam(required = false) Integer page,
                         Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        return "book/search";
    }

    @GetMapping("/book/detail/{idx}/{keyword}/{searchPage}")
    public String detail(@PathVariable int idx, @PathVariable String keyword,
                         @PathVariable int searchPage, Model model,
                         @RequestParam(defaultValue = "0") int page) {
        BookDetailDTO dto = cacheService.getBookDetail(idx, keyword, searchPage);
        if (dto.getId() != null) {
            Page<ReviewDTO> reviewsPage = reviewService.getReviewsByBookId(dto.getId(), page);
            dto = bookDetailService.buildReview(dto, reviewsPage, page);
        }
        model.addAttribute("book", dto);
        return "book/detail";
    }

    @GetMapping("/book/detail/{id}")
    public String detail(@PathVariable Long id, Model model,
                         @RequestParam(defaultValue = "0") int page) {
        BookDetailDTO bookDetailDTO = cacheService.getPopBookDetail(id);
        Page<ReviewDTO> reviewsPage = reviewService.getReviewsByBookId(bookDetailDTO.getId(), page);
        BookDetailDTO dto  = bookDetailService.buildReview(bookDetailDTO, reviewsPage, page);
        model.addAttribute("book", dto);
        return "book/detail";
    }

    @GetMapping("/book/review")
    public String create() {
        return "review/create";
    }


}
