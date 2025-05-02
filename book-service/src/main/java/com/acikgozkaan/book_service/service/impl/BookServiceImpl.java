package com.acikgozkaan.book_service.service.impl;

import com.acikgozkaan.book_service.dto.BookRequest;
import com.acikgozkaan.book_service.dto.BookResponse;
import com.acikgozkaan.book_service.entity.Book;
import com.acikgozkaan.book_service.repository.BookRepository;
import com.acikgozkaan.book_service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public BookResponse create(BookRequest request) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .publicationYear(request.publicationYear())
                .genre(request.genre())
                .stock(request.stock())
                .build();

        return toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse getById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
        return toResponse(book);
    }

    @Override
    public Page<BookResponse> getAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public BookResponse update(UUID id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPublicationYear(request.publicationYear());
        book.setGenre(request.genre());
        book.setStock(request.stock());

        return toResponse(bookRepository.save(book));
    }

    @Override
    public void delete(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Book not found");
        }
        bookRepository.deleteById(id);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getGenre(),
                book.getStock()
        );
    }
}