package com.example.bookstore.service;

import com.example.bookstore.dto.BookDetailDTO;
import com.example.bookstore.dto.NaverBookDTO;
import com.example.bookstore.dto.ReviewDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BookDetailService {
    private final BookRepository bookRepository;
    private final MapperService mapperService;
    private final NaverBookService naverBookService;

    public BookDetailService(BookRepository bookRepository, MapperService mapperService, NaverBookService naverBookService) {
        this.bookRepository = bookRepository;
        this.mapperService = mapperService;
        this.naverBookService = naverBookService;
    }


    public BookDetailDTO buildBookDetail(NaverBookDTO book) {
        Book b = null;
        if (book.getIsbn() != null) {
            Optional<Book> optionalBook = bookRepository.findByIsbn(book.getIsbn());
            if (optionalBook.isPresent()) {
                b = optionalBook.get();

            }
        } else {
            List<Book> candidates = bookRepository.findByTitleAndAuthorAndPublisher(book.getTitle(), book.getAuthor(), book.getPublisher());
            if (!candidates.isEmpty()) {
                b = candidates.get(0);
            }
        }
        if (b == null) {
            return mapperService.naverToDetail(book);
        }
        String description = naverBookService.getDescription(mapperService.bookToDTO(b));
        return mapperService.bookToDetail(b, description);
    }

    public List<BookDetailDTO> fetchDescriptionAndMakeDetailDTO(List<Book> books) {
        List<BookDetailDTO> bookDetailDTOs = new ArrayList<>();
        for (Book book : books) {
            String description = naverBookService.getDescription(mapperService.bookToDTO(book));
            bookDetailDTOs.add(mapperService.bookToDetail(book, description));
        }
        return bookDetailDTOs;
    }

    public BookDetailDTO buildReview(BookDetailDTO dto, Page<ReviewDTO> reviews, int page) {
        dto.setReviews(reviews.getContent());
        dto.setReviewed(!dto.getReviews().isEmpty());
        dto.setCurrentPage(page);  // 현재 페이지
        dto.setTotalPages(reviews.getTotalPages());  // 총 페이지 수
        return dto;
    }

}