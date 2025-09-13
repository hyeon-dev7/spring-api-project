package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.dto.BookResponseDTO;
import com.example.bookstore.dto.SyncResult;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BookSyncService {
    private final String apiKeyEn;
    private final String baseUrl;
    private final String defaultPath;
    private final WebClient webClient;

    private final BookRepository bookRepository;
    private final BookTransactionService bookTransactionService;

    public BookSyncService(@Value("${public-data.api.key-encoding}") String apiKeyEn,
                           @Value("${public-data.api.base-url}") String baseUrl,
                           @Value("${public-data.api.path}") String defaultPath,
                           WebClient.Builder builder,
                           BookRepository bookRepository,
                           BookTransactionService bookTransactionService) {
        this.apiKeyEn = apiKeyEn;
        this.baseUrl = baseUrl;
        this.defaultPath = defaultPath;
        this.bookRepository = bookRepository;
        this.bookTransactionService = bookTransactionService;

        // DefaultUriBuilderFactory 객체를 사용해서 인코딩 안하도록(NONE) 설정
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        // WebClient 생성 시, 해당 설정을 반영
        this.webClient = builder.uriBuilderFactory(factory)
                .baseUrl(this.baseUrl)
                .build();
    }
    // 기본 path 사용
    public List<BookDTO> fetchBookData(int page, int perPage) {
        return fetchBookData(defaultPath, page, perPage);
    }

    // 외부에서 path 주입
    public List<BookDTO> fetchBookData(String path, int page, int perPage) {
        String url = baseUrl + path + "?page=" + page
                + "&perPage=" + perPage
                + "&returnType=JSON"
                + "&serviceKey=" + apiKeyEn;

        return webClient.get()
                .uri(url)  // URL을 직접 사용
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        BookResponseDTO books = objectMapper.readValue(response, BookResponseDTO.class);
//                        log.info(books.toString());
                        return books.getData();
                    }
                    catch (Exception e) {
                        log.error("JSON 파싱 오류: {}", e.getMessage());
                        return null;
                    }
                })
                .block();  // block()을 사용해 동기적으로 결과 기다림
    }

    public SyncResult syncBooks(List<BookDTO> books, String quarter){

        try {
            bookRepository.initBorrowedCount();
        } catch (Exception e) {
            log.error("초기화 실패 :", e);
            return new SyncResult(null, null, null);
        }

        int newBook = 0;
        int updated = 0;
        int errors = 0;

        // 없는 책만 저장, 이미 있는 책은 대출횟수만 업뎃
        for (BookDTO bookDTO : books) {
            try {
                int borrowedCount = bookDTO.getBorrowedCount() != null
                        ? Integer.parseInt(bookDTO.getBorrowedCount()) : 0;
                Book b;
                if (bookDTO.getIsbn() != null) {
                    Optional<Book> optionalBook = bookRepository.findByIsbn(bookDTO.getIsbn());
                    if (optionalBook.isPresent()) {
                        b = bookTransactionService.updateBook(bookDTO, optionalBook.get(), borrowedCount);
                        updated++;
                    } else {
                        b = bookTransactionService.saveNewBook(bookDTO, borrowedCount);
                        newBook++;
                    }
                } else {
                    List<Book> candidates = bookRepository.findByTitleAndAuthorAndPublisher(bookDTO.getTitle(), bookDTO.getAuthor(), bookDTO.getPublisher());
                    if (candidates.isEmpty()) {
                        b = bookTransactionService.saveNewBook(bookDTO, borrowedCount);
                        newBook++;

                    } else {
                        b = bookTransactionService.updateBook(bookDTO, candidates.get(0), borrowedCount);
                        updated++;
                    }
                }

                try {
                    bookTransactionService.savePopularity(b, quarter);
                } catch (Exception e){
                    log.warn("Popularity 저장 실패 - 책 제목: {}, ISBN: {}, 오류: {}", b.getTitle(), b.getIsbn(), e.getMessage());
                    errors++;
                }

            }
            catch (Exception e) {
                errors++;
                log.error("Book 저장 실패 - 제목: {}, ISBN: {}, 오류: {}",
                        bookDTO.getTitle(), bookDTO.getIsbn(), e.getMessage());
            }
        }
        return new SyncResult(newBook, updated, errors);
    }

}
