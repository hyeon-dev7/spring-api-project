package com.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDTO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String nickname;
    @NotBlank(message="후기를 입력해 주세요. ")
    private String content;
    @Range(min = 1, max = 5, message="별점을 선택해 주세요.")
    private int stars;
    private LocalDate createdDate;

    private NaverBookDTO bookData;
    // 네이버 책 후기일 경우 캐시 삭제할 때 필요
    private Integer idx;
    private String keyword;
}
