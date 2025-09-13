package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Popularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PopularityRepository extends JpaRepository<Popularity, Long> {
    List<Popularity> findAllByBook(Book book);
    @Query(value = "SELECT quarter, SUM(counts) FROM popularity GROUP BY quarter", nativeQuery = true)
    List<Object[]> sumGroupByQuarter();

}
