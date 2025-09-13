package com.example.bookstore.service;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.dto.NaverBookDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Popularity;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.PopularityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookTransactionService {

    private final BookRepository bookRepository;
    private final PopularityRepository popularityRepository;

    public BookTransactionService(BookRepository bookRepository, PopularityRepository popularityRepository) {
        this.bookRepository = bookRepository;
        this.popularityRepository = popularityRepository;
    }



    @Transactional
    public Book updateBook(BookDTO bookDTO, Book book, int borrowedCount){
        book.setBorrowedCount(borrowedCount);
        if(book.getLocation()==null){
            book.setLocation(bookDTO.getLocation());
        }
        if(book.getCall_num()==null){
            book.setCall_num(bookDTO.getCallNumber());
        }
        return bookRepository.save(book);
    }
    @Transactional
    public Book saveNewBook(BookDTO bookDTO, int borrowedCount){
        Book b = Book.builder().title(bookDTO.getTitle()).author(bookDTO.getAuthor())
                .call_num(bookDTO.getCallNumber()).borrowedCount(borrowedCount).location(bookDTO.getLocation())
                .publisher(bookDTO.getPublisher()).isbn(bookDTO.getIsbn()).image(bookDTO.getImage()).build();
        return bookRepository.save(b);
    }

    @Transactional
    public void savePopularity(Book book, String quarter){
        Popularity pop = Popularity.builder()
                .book(book).quarter(quarter).counts(book.getBorrowedCount())
                .build();
        popularityRepository.save(pop);
    }

    // createReview의 트랜잭션 안에서 실행 -> 트랜잭션 관리 됨
    public Book getOrCreateBook(Long bookId, NaverBookDTO bookData) {
        if (bookId != null) {
            return bookRepository.findById(bookId)
                    .orElseThrow(()-> new RuntimeException("책 정보를 찾을 수 없습니다."));
        }
        Book book = Book.builder()
                .title(bookData.getTitle())
                .author(bookData.getAuthor())
                .publisher(bookData.getPublisher())
                .image(bookData.getImage())
                .isbn(bookData.getIsbn())
                .build();
        return bookRepository.save(book);

    }
}

