package com.example.bookstore.service;

import com.example.bookstore.config.CacheConfig;
import com.example.bookstore.dto.BookDetailDTO;
import com.example.bookstore.dto.BookResponseDTO;
import com.example.bookstore.dto.NaverBookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.PopularityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CacheService {

    private final CacheConfig cacheConfig;
    private final BookRepository bookRepository;
    private final BookDetailService bookDetailService;
    private final PopularityRepository popularityRepository;

    public CacheService(CacheConfig cacheConfig, BookRepository bookRepository, BookDetailService bookDetailService, PopularityRepository popularityRepository) {
        this.cacheConfig = cacheConfig;
        this.bookRepository = bookRepository;
        this.bookDetailService = bookDetailService;
        this.popularityRepository = popularityRepository;
    }

    public NaverBookDTO getCachedBookByIdx(int idx, String keyword, int page) {
        Cache cache = cacheConfig.cacheManager().getCache("bookSearchCache");
        if (cache != null) {
            String key = keyword.replaceAll("\\s+", "").toLowerCase() + "_" + page;
            Cache.ValueWrapper wrapper = cache.get(key);
            // Cache.ValueWrapper : 캐시된 값을 감싸는 인터페이스
            if (wrapper != null && wrapper.get() != null) {
                BookResponseDTO bookResponseDTO = (BookResponseDTO) wrapper.get();
                for (NaverBookDTO book : bookResponseDTO.getItems()){
                    if (idx == book.getIdx()){
                        return book;
                    }
                }
            }
        }
        return null;
    }

    public BookDetailDTO getDetailFromCachedPopBook(Long id, String location) {
        Cache cache = cacheConfig.cacheManager().getCache("popularBooks");
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(location);
            List<BookDetailDTO> bookDetailDTOs = new ArrayList<>();
            if (wrapper == null || wrapper.get() == null) {
                bookDetailDTOs.addAll(getPopularBooksByLoc(location));
                cache.put(location, bookDetailDTOs);
            }
            else {
                bookDetailDTOs.addAll((List<BookDetailDTO>) wrapper.get());
            }
            for (BookDetailDTO dto : bookDetailDTOs){
                if (id.equals(dto.getId())){
                    return dto;
                }
            }
        }
        return null;
    }


    @Cacheable(value = "home_books")
    public List<BookDetailDTO> getHomeBooks() {
        List<Book> fiveBooks = bookRepository.getFiveBooks();
        return bookDetailService.fetchDescriptionAndMakeDetailDTO(fiveBooks);
    }

    @Cacheable(value = "chartData")
    public Map<String, Object> getChartData(){
        Map<String, Object> data = new HashMap<>();
        Map<String, Integer> byLoc = new LinkedHashMap<>(); // 순서 유지
        byLoc.put("종합+스마트", bookRepository.findPopBooksByLocation("종합").size());
        byLoc.put("어린이실", bookRepository.findPopBooksByLocation("어린이").size());
        byLoc.put("범사이상희문고", bookRepository.findPopBooksByLocation("범사").size());

        Map<String, Integer> byQuarter = new LinkedHashMap<>();
        for (Object[] row : popularityRepository.sumGroupByQuarter()){
            byQuarter.put((String) row[0], ((Number) row[1]).intValue());
        }
        data.put("loc", byLoc);
        data.put("quarter", byQuarter);
        return data;
    }


    @Cacheable(value = "popularBooks", key ="#location")
    public List<BookDetailDTO> getPopularBooksByLoc(String location) {
        List<Book> books = new ArrayList<>();
        if (location.equals("G")) {
            books.addAll(bookRepository.findPopBooksByLocation("종합"));
            books.addAll(bookRepository.findPopBooksByLocation("스마트"));
        } else if(location.equals("B")) {
            books.addAll(bookRepository.findPopBooksByLocation("범사"));
        } else {
            books.addAll(bookRepository.findPopBooksByLocation("어린이"));
        }
        if (books.isEmpty()) {
            return new ArrayList<>();
        }
        return bookDetailService.fetchDescriptionAndMakeDetailDTO(books);
    }

    @Cacheable(value = "bookDetailCache", key = "#idx + '_' + #keyword.replaceAll('\\s+', '').toLowerCase()")
    public BookDetailDTO getBookDetail(int idx, String keyword, int page) {
        NaverBookDTO book = getCachedBookByIdx(idx, keyword, page);
        return book != null ? bookDetailService.buildBookDetail(book) : new BookDetailDTO();
    }

    @CacheEvict(value = "bookDetailCache", key = "#idx + '_' + #keyword.replaceAll('\\s+', '').toLowerCase()")
    public void evictNaverBookDetailCache(int idx, String keyword) {} // 캐시 삭제


    @Cacheable(value = "popBookDetailCache", key="#id")
    public BookDetailDTO getPopBookDetail(Long id) {
        Optional<Book> book = bookRepository.findById(id);
        if (book.isEmpty()) {
            return new BookDetailDTO();
        }

        String loc;
        if(book.get().getLocation().contains("어린이")){
            loc = "C";
        } else if(book.get().getLocation().contains("범사")){
            loc ="B";
        } else {
            loc = "G";
        }
        return getDetailFromCachedPopBook(id, loc);
    }

}
