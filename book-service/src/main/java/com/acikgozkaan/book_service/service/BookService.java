package com.acikgozkaan.book_service.service;

import com.acikgozkaan.book_service.dto.BookRequest;
import com.acikgozkaan.book_service.dto.BookResponse;

import java.util.List;
import java.util.UUID;

public interface BookService {

    BookResponse create(BookRequest request);

    BookResponse getById(UUID id);

    List<BookResponse> getAll();

    BookResponse update(UUID id, BookRequest request);

    void delete(UUID id);

}