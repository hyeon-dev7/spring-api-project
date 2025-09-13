package com.example.bookstore.service;

import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Review;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.ReviewRepository;
import com.example.bookstore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final MapperService mapperService;
    private final CacheService cacheService;
    private final BookTransactionService bookTransactionService;

    public ReviewService(ReviewRepository reviewRepository, BookRepository bookRepository, UserRepository userRepository, MapperService mapperService, CacheService cacheService, BookTransactionService bookTransactionService) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.mapperService = mapperService;
        this.cacheService = cacheService;
        this.bookTransactionService = bookTransactionService;
    }

    @Transactional
    public Review createReview(ReviewDTO reviewDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 정보 조회 실패" + username));

        Book book = bookTransactionService.getOrCreateBook(reviewDTO.getBookId(), reviewDTO.getBookData());
        return saveReview(reviewDTO, user, book);
    }

    public Review saveReview(ReviewDTO reviewDTO, User user, Book book) {
        if (reviewDTO.getIdx() != null && reviewDTO.getKeyword() != null) {
            cacheService.evictNaverBookDetailCache(reviewDTO.getIdx(), reviewDTO.getKeyword());
        }
        return reviewRepository.save(mapperService.dtoToReview(reviewDTO, user, book));
    }

    public Page<ReviewDTO> getReviewsByBookId(Long bookId, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Review> reviews = reviewRepository.findAllByBookIdOrderByCreatedDateDesc(bookId, pageable);
        if (reviews.isEmpty()) {
            return Page.empty();
        }
        return mapperService.reviewsToDTO(reviews);
    }

    public Page<ReviewDTO> getReviewsByUserId(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAllByUserIdOrderByCreatedDateDesc(userId, pageable);
        if (reviews.isEmpty()) {
            return Page.empty();
        }
        Page<ReviewDTO> reviewDTOs = mapperService.reviewsToDTO(reviews);
        for (ReviewDTO reviewDTO : reviewDTOs) {
            Optional<Book> book = bookRepository.findById(reviewDTO.getBookId());
            if (book.isPresent()) {
                reviewDTO.setBookData(mapperService.forMyReview(book.get()));
            }
        }
        return reviewDTOs;
    }

    public boolean deleteReview(Long reviewId, Long userId) {
        if (!reviewer(reviewId, userId)) {
            return false;
        }
        try {
            Review review = reviewRepository.findById(reviewId).get();
            reviewRepository.delete(review);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean reviewer(Long reviewId, Long userId) {
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if (optionalReview.isPresent() && optionalReview.get().getUser().getId().equals(userId)) {
            return true;
        }
        return false;
    }

    public boolean updateReview(ReviewDTO reviewDTO, Long reviewId, Long userId) {
        if (reviewer(reviewId, userId) && Objects.equals(reviewDTO.getId(), reviewId)) {
            try {
                Review review = reviewRepository.findById(reviewId).get();
                review.setContent(reviewDTO.getContent());
                review.setStars(reviewDTO.getStars());
                reviewRepository.save(review);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

}
