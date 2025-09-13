package com.example.bookstore.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CacheType {
    BOOK_SEARCH("bookSearchCache", 3, 1000),
    BOOK_DETAIL("bookDetailCache", 3, 500),

    BOOK_HOME("home_books", null, 5),
    BOOK_POPULAR("popularBooks", null, 250),
    // 캐시 키로 구분.. G : 종합 + 스마트, B : 범사, C : 어린이
    BOOK_POP_DETAIL("popBookDetailCache", null, 250),

    BOOK_CHART_DATA("chartData", null, 10);

    private final String cacheName;
    private final Integer  expireAfterAccess;
    private final int maximumSize;

}
