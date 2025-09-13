package com.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 필요한 필드만 가져오는 법
public class BookResponseDTO {
    @JsonProperty("data")
    private List<BookDTO> data;

    @JsonProperty("items")
    private List<NaverBookDTO> items;
    private int total;
}
