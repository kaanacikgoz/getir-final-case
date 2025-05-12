package com.acikgozkaan.book_service.config;

import com.acikgozkaan.book_service.entity.Book;
import com.acikgozkaan.book_service.entity.Genre;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecifications {

    public static Specification<Book> withTitle(String title) {
        return (root, query, cb) ->
                title == null ? null : cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Book> withAuthor(String author) {
        return (root, query, cb) ->
                author == null ? null : cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
    }

    public static Specification<Book> withIsbn(String isbn) {
        return (root, query, cb) ->
                isbn == null ? null : cb.like(cb.lower(root.get("isbn")), "%" + isbn.toLowerCase() + "%");
    }

    public static Specification<Book> withGenre(Genre genre) {
        return (root, query, cb) ->
                genre == null ? null : cb.equal(root.get("genre"), genre);
    }
}