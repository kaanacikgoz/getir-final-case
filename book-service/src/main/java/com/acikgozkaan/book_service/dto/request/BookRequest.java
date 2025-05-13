package com.acikgozkaan.book_service.dto;

import com.acikgozkaan.book_service.constant.BookConstants;
import com.acikgozkaan.book_service.entity.Genre;
import jakarta.validation.constraints.*;

public record BookRequest(

        @NotBlank(message = "Title cannot be empty")
        @Size(max = 100, message = "Title cannot exceed 100 characters")
        String title,

        @NotBlank(message = "Author name cannot be empty")
        @Size(max = 75, message = "Author name cannot exceed 75 characters")
        String author,

        @NotBlank(message = "ISBN cannot be empty")
        @Size(min=13, max = 13, message = "ISBN must be exactly 13 characters")
        String isbn,

        @Min(value = BookConstants.MIN_PUBLICATION_YEAR, message = "Publication year must be after 1450")
        @Max(value = BookConstants.MAX_PUBLICATION_YEAR, message = "Publication year cannot be in the future")
        int publicationYear,

        @NotNull(message = "Genre must be specified")
        Genre genre,

        @Min(value = 0, message = "Stock cannot be negative")
        int stock

) {}