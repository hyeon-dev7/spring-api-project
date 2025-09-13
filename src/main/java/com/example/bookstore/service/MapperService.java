package com.example.bookstore.service;

import com.example.bookstore.dto.*;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Popularity;
import com.example.bookstore.entity.Review;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.PopularityRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MapperService {

    private final PopularityRepository popularityRepository;

    public MapperService(PopularityRepository popularityRepository) {
        this.popularityRepository = popularityRepository;
    }

    public BookDetailDTO bookToDetail(Book book, String description) {
        List<Popularity> popList = popularityRepository.findAllByBook(book);
        String popularityJson = "{}";
        try {
            if (popList != null && !popList.isEmpty()) {
                popularityJson = new ObjectMapper().writeValueAsString(popList);
            }
        } catch (JsonProcessingException e) {
            log.warn("detailDto JSON 변환 실패", e);
        }
        return BookDetailDTO.builder()
                .id(book.getId())
                .image(book.getImage())
                .title(book.getTitle())
                .author(book.getAuthor())
                .location(book.getLocation())
                .callNumber(book.getCall_num())
                .publisher(book.getPublisher())
                .borrowedCount(String.valueOf(book.getBorrowedCount()))
                .popularityJson(popularityJson)
                .description(description)
                .build();
    }

    public BookDetailDTO naverToDetail(NaverBookDTO book) {
        return BookDetailDTO.builder()
                .id(null)
                .image(book.getImage())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .reviewed(false)
                .popularityJson("{}")
                .build();
    }

    public BookDTO bookToDTO(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .image(book.getImage())
                .location(book.getLocation())
                .callNumber(book.getCall_num())
                .borrowedCount(String.valueOf(book.getBorrowedCount()))
                .build();
    }

    public UserDTO userToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setNickname(user.getNickname());
        return userDTO;
    }

    public List<Map<String, String>> mapValidationErrors(BindingResult bindingResult){
        return bindingResult.getFieldErrors().stream()
                .map(err-> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage()
                )).toList();
    }

    public Review dtoToReview(ReviewDTO dto, User user, Book book) {
        return Review.builder()
                .content(dto.getContent())
                .user(user)
                .book(book)
                .stars(dto.getStars())
                .build();
    }

    public Page<ReviewDTO> reviewsToDTO(Page<Review> reviews){
        List<ReviewDTO> reviewDTOList = reviews.getContent().stream()
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .content(review.getContent())
                        .stars(review.getStars())
                        .bookId(review.getBook().getId())
                        .userId(review.getUser().getId())
                        .createdDate(review.getCreatedDate())
                        .nickname(review.getUser().getNickname())
                        .build()
        ).toList();
        // 리스트를 page 객체로 변환 (데이터, 페이지 정보-페이지 번호, 크기 등-, 전체 요소 수)
        return new PageImpl<>(reviewDTOList, reviews.getPageable(), reviews.getTotalElements());
    }

    public NaverBookDTO forMyReview(Book book) {
        return NaverBookDTO.builder()
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .image(book.getImage())
                .build();
    }
}
