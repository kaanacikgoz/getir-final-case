package com.acikgozkaan.book_service.controller;

import com.acikgozkaan.book_service.dto.BookRequest;
import com.acikgozkaan.book_service.dto.BookResponse;
import com.acikgozkaan.book_service.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(201).body(bookService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable("id") UUID id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

}