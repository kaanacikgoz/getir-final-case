package com.acikgozkaan.book_service.service.impl;

import com.acikgozkaan.book_service.dto.BookRequest;
import com.acikgozkaan.book_service.dto.BookResponse;
import com.acikgozkaan.book_service.entity.Book;
import com.acikgozkaan.book_service.exception.BookNotFoundException;
import com.acikgozkaan.book_service.exception.IsbnAlreadyExistsException;
import com.acikgozkaan.book_service.mapper.BookMapper;
import com.acikgozkaan.book_service.repository.BookRepository;
import com.acikgozkaan.book_service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookResponse create(BookRequest request) {
        validateIsbnUniquenessOnCreate(request.isbn());
        Book book = bookMapper.toBook(request);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse getById(UUID id) {
        return bookMapper.toResponse(findBookById(id));
    }

    @Override
    public List<BookResponse> getAll() {
        return bookRepository.findAll()
                .stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public BookResponse update(UUID id, BookRequest request) {
        validateIsbnUniquenessOnUpdate(request.isbn(), id);
        Book book = findBookById(id);
        bookMapper.updateBookFromRequest(book, request);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    public void delete(UUID id) {
        Book book = findBookById(id);
        bookRepository.delete(book);
    }

    private Book findBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private void validateIsbnUniquenessOnCreate(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new IsbnAlreadyExistsException(isbn);
        }
    }

    private void validateIsbnUniquenessOnUpdate(String isbn, UUID id) {
        if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new IsbnAlreadyExistsException(isbn);
        }
    }

}