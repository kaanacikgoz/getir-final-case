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

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(201).body(bookService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    public ResponseEntity<BookResponse> getBookById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(bookService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable("id") UUID id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<Void> decreaseStock(@PathVariable("id") UUID id) {
        bookService.decreaseStock(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<Void> increaseStock(@PathVariable("id") UUID id) {
        bookService.increaseStock(id);
        return ResponseEntity.noContent().build();
    }

}