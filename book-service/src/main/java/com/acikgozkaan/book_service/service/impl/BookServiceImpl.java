package com.acikgozkaan.book_service.service.impl;

import com.acikgozkaan.book_service.config.BookSpecifications;
import com.acikgozkaan.book_service.config.StockUpdatePublisher;
import com.acikgozkaan.book_service.dto.request.BookRequest;
import com.acikgozkaan.book_service.dto.response.BookResponse;
import com.acikgozkaan.book_service.dto.BookStockEvent;
import com.acikgozkaan.book_service.entity.Book;
import com.acikgozkaan.book_service.entity.Genre;
import com.acikgozkaan.book_service.exception.BookNotFoundException;
import com.acikgozkaan.book_service.exception.IsbnAlreadyExistsException;
import com.acikgozkaan.book_service.exception.OutOfStockException;
import com.acikgozkaan.book_service.mapper.BookMapper;
import com.acikgozkaan.book_service.repository.BookRepository;
import com.acikgozkaan.book_service.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final StockUpdatePublisher stockUpdatePublisher;

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
    public Page<BookResponse> searchBooks(String title, String author, String isbn, Genre genre, Pageable pageable) {
        Specification<Book> spec = Specification
                .where(BookSpecifications.withTitle(title))
                .and(BookSpecifications.withAuthor(author))
                .and(BookSpecifications.withIsbn(isbn))
                .and(BookSpecifications.withGenre(genre));

        return bookRepository.findAll(spec, pageable)
                .map(bookMapper::toResponse);
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

    @Override
    public void decreaseStock(UUID id) {
        Book book = findBookById(id);

        if (book.getStock() <= 0) {
            throw new OutOfStockException(id);
        }

        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        stockUpdatePublisher.publish(
                new BookStockEvent(book.getId(), book.getTitle(), book.getStock())
        );
    }

    @Override
    public void increaseStock(UUID id) {
        Book book = findBookById(id);
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        stockUpdatePublisher.publish(
                new BookStockEvent(book.getId(), book.getTitle(), book.getStock())
        );
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