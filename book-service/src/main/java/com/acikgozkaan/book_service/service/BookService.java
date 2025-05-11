package com.acikgozkaan.book_service.service;

import com.acikgozkaan.book_service.dto.BookRequest;
import com.acikgozkaan.book_service.dto.BookResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface BookService {

    BookResponse create(BookRequest request);

    BookResponse getById(UUID id);

    List<BookResponse> getAll();

    Page<BookResponse> searchBooks(String title, String author, String isbn, String genre, int page, int size);

    BookResponse update(UUID id, BookRequest request);

    void delete(UUID id);

    void decreaseStock(UUID id);

    void increaseStock(UUID id);
}