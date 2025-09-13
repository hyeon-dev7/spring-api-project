package com.example.bookstore.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BookDetailDTO {

    private Long id;

    private String title;
    private String author;
    private String publisher;
    private String callNumber;
    private String location;
    private String borrowedCount;
    private String image;
    private String isbn;

    private String description; // 네이버 api 에서 가져오기

    private List<ReviewDTO> reviews;
    private boolean reviewed;
    private int currentPage;
    private int totalPages;

    private String popularityJson;
}