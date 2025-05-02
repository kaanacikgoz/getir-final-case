package com.acikgozkaan.book_service.dto;

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
        @Size(max = 13)
        String isbn,

        @Min(value = 1450, message = "Publication year must be after 1450")
        @Max(value = 2025, message = "Publication year cannot be in the future")
        int publicationYear,

        @NotNull(message = "Genre must be specified")
        Genre genre,

        @Min(value = 0, message = "Stock cannot be negative")
        int stock

) {}