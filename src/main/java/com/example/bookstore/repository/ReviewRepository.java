package com.example.bookstore.repository;

import com.example.bookstore.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByBookIdOrderByCreatedDateDesc(Long bookId, Pageable pageable);
    Page<Review> findAllByUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);
}
