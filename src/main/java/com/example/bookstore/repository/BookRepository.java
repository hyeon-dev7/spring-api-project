package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findById(Long id);
    Optional<Book> findByIsbn(String isbn);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.borrowedCount = null WHERE b.borrowedCount IS NOT NULL")
    int initBorrowedCount(); // nativeQuery 를 안 쓸 경우 Entity 기준으로 변수명 작성

    @Query(value = "SELECT * FROM book WHERE REPLACE(title, ' ','') = REPLACE(:title, ' ', '') " +
            "AND REPLACE(author, ' ', '') = REPLACE(:author, ' ', '') " +
            "AND REPLACE(publisher, ' ', '') = REPLACE(:publisher, ' ', '')",
            nativeQuery = true)
    List<Book> findByTitleAndAuthorAndPublisher(@Param("title") String title,
                                                @Param("author") String author,
                                                @Param("publisher") String publisher);
    @Query(value = "SELECT * FROM book WHERE image IS NOT NULL ORDER BY borrowed_count DESC LIMIT 5", nativeQuery = true)
    List<Book> getFiveBooks();

    @Query(value ="SELECT * FROM book WHERE location LIKE %:location% AND borrowed_count IS NOT NULL ORDER BY borrowed_count DESC", nativeQuery = true)
    List<Book> findPopBooksByLocation(@Param("location") String location);
}
