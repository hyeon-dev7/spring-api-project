package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.dto.BookResponseDTO;
import com.example.bookstore.dto.NaverBookDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NaverBookService {
    private static final String BASE_PATH = "/v1/search/book.json";

    private final String clientId;
    private final String clientSecret;
    private final WebClient webClient;

    public NaverBookService(@Value("${naver.api.client-id}") String clientId,
                            @Value("${naver.api.client-secret}") String clientSecret,
                            WebClient.Builder builder) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.webClient = builder.baseUrl("https://openapi.naver.com").build();
    }

    public Flux<BookDTO> fetchImageIsbn(List<BookDTO> books) {
        List<Mono<BookDTO>> updatedBooks = new ArrayList<>();

        for (BookDTO book : books) {
            Mono<BookDTO> updatedBookMono = fetchIIByTitle(book.getTitle())
                    .map(update -> { // .map 으로 비동기식 처리
                        if (update != null) {
                            book.setImage(update.getImage());
                            book.setIsbn(update.getIsbn());
                        }
                        return book;
                    });

            updatedBooks.add(updatedBookMono);
        }

        return Flux.concat(updatedBooks) // Flux.concat() : 여러 Mono를 순차적으로 실행, 하나의 요청이 완료된 후 다음 요청이 시작됩
                .delayElements(Duration.ofMillis(300)); // 300ms 간격으로 요청
    }

    private Mono<BookDTO> fetchIIByTitle(String title) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(BASE_PATH)
                            .queryParam("query", title)
                            .build())
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        try {
                            log.info("Response received: {}", response);

                            ObjectMapper objectMapper = new ObjectMapper();
                            JsonNode rootNode = objectMapper.readTree(response);
                            JsonNode itemNode = rootNode.path("items");

                            if (!itemNode.isEmpty()) {
                                JsonNode item = findMatchingBook(itemNode, normalizedWord(title));
                                return new BookDTO(item.get("image").asText(), item.get("isbn").asText());
                            }
                        } catch (IOException e) {
                            log.error("JSON 파싱 오류: {}", e.getMessage());
                        }
                        return new BookDTO(null, null);
                    });
        } catch (Exception e) {
            log.error("URL 인코딩 오류: {}", e.getMessage());
            return Mono.empty();
        }
    }

    private JsonNode findMatchingBook(JsonNode items, String normalizedTitle) {
        for (JsonNode item : items) {
            String itemTitle = item.path("title").asText();
            if (normalizedTitle.equals(normalizedWord(itemTitle))) {
                return item;
            }
        }
        // 일차하는 책이 없으면.. 우선 첫번째 결과 반환
        return items.get(0);
    }

    public Mono<BookResponseDTO> search(String keyword, int page) {
        int display = 20;
        int start = page * display + 1;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_PATH)
                        .queryParam("query", keyword)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(BookResponseDTO.class)
                // HTTP 응답 본문(body)을 하나의 객체(Mono)로 변환
                .map(response -> {
                    List<NaverBookDTO> items = response.getItems();
                    if (items == null || items.isEmpty()) {
                        return response;
                    }
                    for (int i = 0; i < items.size(); i++) {
                        items.get(i).setIdx(start + i);
                    }
                    System.out.println(response.getTotal());
                    return response;
                });
    }

    public String getDescription(BookDTO bookDTO) {
        String key;
        if (bookDTO.getIsbn() != null && !bookDTO.getIsbn().trim().isEmpty()) {
            key = bookDTO.getIsbn();
        } else {
            key = normalizedWord(bookDTO.getTitle());
        }

        try {
            Thread.sleep(200);  // 200ms 간격으로 호출
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // 인터럽트 처리
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_PATH)
                        .queryParam("query", key)
                        .build())
                .header("X-Naver-Client-Id", clientId)
                .header("X-Naver-Client-Secret", clientSecret)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        log.info("Response received: {}", response);

                        ObjectMapper objectMapper = new ObjectMapper();
                        JsonNode rootNode = objectMapper.readTree(response);
                        JsonNode items = rootNode.path("items");

                        if (!items.isEmpty()) {
                            JsonNode item;
                            if (bookDTO.getIsbn()!=null){
                                item = items.get(0);
                            } else{
                                item = findMatchingBook(items, key);
                            }
                            return item.path("description").asText();
                        }
                    } catch (IOException e) {
                        log.error("JSON 파싱 오류: {}", e.getMessage());
                    }
                    return "";
                }).block();
    }


    private String normalizedWord(String word){
        return word.replaceAll("\\s+", "").toLowerCase();
    }

}