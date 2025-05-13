package com.acikgozkaan.book_service.dto.response;

import com.acikgozkaan.book_service.entity.Genre;

import java.util.UUID;

public record BookResponse(

        UUID id,
        String title,
        String author,
        String isbn,
        int publicationYear,
        Genre genre,
        int stock

) {}