package com.acikgozkaan.book_service.repository;

import com.acikgozkaan.book_service.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    //Optional<Book> findByIsbn(String isbn);

    //boolean existsByIsbn(String isbn);
}