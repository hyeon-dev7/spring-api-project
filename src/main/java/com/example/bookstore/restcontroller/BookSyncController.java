package com.example.bookstore.restcontroller;

import com.example.bookstore.config.CacheConfig;
import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.dto.SyncResult;

import com.example.bookstore.service.BookSyncService;
import com.example.bookstore.service.CacheService;
import com.example.bookstore.service.NaverBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BookSyncController {

    private final BookSyncService bookSyncService;
    private final NaverBookService naverBookService;
    private final CacheConfig cacheConfig;
    private final CacheService cacheService;

    public BookSyncController(BookSyncService bookSyncService, NaverBookService naverBookService, CacheConfig cacheConfig, CacheService cacheService) {
        this.bookSyncService = bookSyncService;
        this.naverBookService = naverBookService;
        this.cacheConfig = cacheConfig;
        this.cacheService = cacheService;
    }

    @PostMapping("/api/sync/books/{quarter}")
    // curl -X POST http://localhost:8080/api/sync/books/2025-Q1?path=~~ 터미널에서 이런식으로 사용 가능
    public ResponseEntity<SyncResult> initBooks(@PathVariable String quarter,
                                                @RequestParam String path) {

        // 1. DB 관리
        List<BookDTO> bookDTOs = bookSyncService.fetchBookData(path, 1,200);
        List<BookDTO> books = naverBookService.fetchImageIsbn(bookDTOs)
                .collectList().block();
            // Flux<BookDTO> → Mono<List<BookDTO>> 비동기적으로 처리된 여러 개의 책 정보를 하나의 리스트로 묶음
            // → List<BookDTO> block으로 결과를 동기적으로 기다림, 리스트 반환
        SyncResult res = bookSyncService.syncBooks(books, quarter);


        // 2. 캐시 삭제
        String[] cacheNames = {"home_books", "popularBooks", "popBookDetailCache"};
        for (String name : cacheNames) {
            cacheConfig.cacheManager().getCache(name).clear();
        }

        // 3. 캐시 생성
        cacheService.getHomeBooks();
        cacheService.getPopularBooksByLoc("G");
        cacheService.getPopularBooksByLoc("B");
        cacheService.getPopularBooksByLoc("C");

        return ResponseEntity.ok(res);
    }


}
