package com.acikgozkaan.book_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 75)
    private String author;

    @Column(unique = true, nullable = false, length = 13)
    private String isbn;

    @Min(1450)
    @Max(2025)
    @Column(nullable = false)
    private int publicationYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genre genre;

    @Min(0)
    @Column(nullable = false)
    private int stock;
}