package com.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data // get set toString
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookDTO {
    private Long id;

    @JsonProperty("서명")
    private String title;

    @JsonProperty("저자")
    private String author;

    @JsonProperty("발행자")
    private String publisher;

    @JsonProperty("청구기호")
    private String callNumber;

    @JsonProperty("자료실")
    private String location;

    @JsonProperty("대출횟수")
    private String borrowedCount;

    // 네이버 API에서 가져오기
    private String image;
    private String isbn;

    public BookDTO(String image, String isbn) {
        this.image = image;
        this.isbn = isbn;
    }

}