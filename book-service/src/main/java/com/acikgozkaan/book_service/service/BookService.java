package com.acikgozkaan.book_service.service;

import com.acikgozkaan.book_service.dto.BookRequest;
import com.acikgozkaan.book_service.dto.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookService {

    BookResponse create(BookRequest request);

    BookResponse getById(UUID id);

    Page<BookResponse> getAll(Pageable pageable);

    BookResponse update(UUID id, BookRequest request);

    void delete(UUID id);

}