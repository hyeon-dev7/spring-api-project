package com.example.bookstore.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="book", indexes = {
        @Index(name="idx_title_author_publisher", columnList = "title, author, publisher"),
        @Index(name = "idx_isbn", columnList = "isbn")
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String title;
    @Column
    private String author;
    @Column(unique = true)
    private String isbn;
    @Column
    private String publisher;
    @Column
    private String location;
    @Column
    private String call_num; // 청구기호
    @Column(name = "borrowed_count")
    private Integer borrowedCount; // 대여 횟수
    @Column
    private String image;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Popularity> popularityRecords = new ArrayList<>();

}
