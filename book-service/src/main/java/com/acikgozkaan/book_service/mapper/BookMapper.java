package com.acikgozkaan.book_service.mapper;

import com.acikgozkaan.book_service.dto.request.BookRequest;
import com.acikgozkaan.book_service.dto.response.BookResponse;
import com.acikgozkaan.book_service.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toBook(BookRequest request) {
        return Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .publicationYear(request.publicationYear())
                .genre(request.genre())
                .stock(request.stock())
                .build();
    }

    public void updateBookFromRequest(Book book, BookRequest request) {
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPublicationYear(request.publicationYear());
        book.setGenre(request.genre());
        book.setStock(request.stock());
    }

    public BookResponse toResponse(Book book) {
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