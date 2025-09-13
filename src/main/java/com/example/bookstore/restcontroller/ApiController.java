package com.example.bookstore.restcontroller;

import com.example.bookstore.dto.BookDetailDTO;
import com.example.bookstore.dto.BookResponseDTO;
import com.example.bookstore.service.CacheService;
import com.example.bookstore.service.NaverBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/books")
public class ApiController {
    private final NaverBookService naverBookService;
    private final CacheService cacheService;

    public ApiController( NaverBookService naverBookService, CacheService cacheService) {
        this.naverBookService = naverBookService;
        this.cacheService = cacheService;
    }


    @GetMapping("")
    public List<BookDetailDTO> home() {
        return cacheService.getHomeBooks();
    }

    @GetMapping("/chart")
    public Map<String, Object> homepage(){
        return cacheService.getChartData();
    }


    @Cacheable(value = "bookSearchCache", key = "#keyword.replaceAll('\\s+', '').toLowerCase() + '_' + #page")
    @GetMapping("/search")
    public BookResponseDTO searchBooks(@RequestParam("keyword") String keyword,
                                       @RequestParam("page") int page){
        return naverBookService.search(keyword, page).block();

    }

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopBookList(
            @RequestParam String loc,
            @RequestParam(defaultValue = "0") int page
    ){
        if (!List.of("G", "B", "C").contains(loc)){
            return ResponseEntity.badRequest()
                    .body(Map.of( "error", "유효하지 않은 location 값입니다."));
        }
        int size = 20;
        List<BookDetailDTO> popBooks = cacheService.getPopularBooksByLoc(loc);
        int total = popBooks.size();
        int totalPages = (int) Math.ceil((double) total/size);
        int start = page * size;
        int end = Math.min(start+size, total);
        Map<String, Object> response = new HashMap<>();
        response.put("totalPages", totalPages);
        response.put("currentPage", page);
        response.put("books", popBooks.subList(start, end));
        return ResponseEntity.ok(response);
    }
}