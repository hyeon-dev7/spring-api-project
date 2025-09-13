package com.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverBookDTO {

    private int idx; // 리스트 인덱스

    private String title;
    private String author;
    private String publisher;

    private String image;
    private String isbn;
    private String description;

}
