package com.acikgozkaan.book_service.repository;

import com.acikgozkaan.book_service.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {

    boolean existsByIsbn(String isbn);
    boolean existsByIsbnAndIdNot(String isbn, UUID id);
}