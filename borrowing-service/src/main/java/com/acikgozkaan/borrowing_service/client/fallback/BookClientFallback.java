package com.acikgozkaan.borrowing_service.client.fallback;

import com.acikgozkaan.borrowing_service.client.BookClient;
import com.acikgozkaan.borrowing_service.dto.external.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class BookClientFallback implements BookClient {
    @Override
    public BookResponse getBookById(UUID id) {
        log.warn("Fallback: book-service not available.");
        return null;
    }

    @Override
    public void decreaseStock(UUID id) {
        log.warn("Fallback: book-service not available.");
    }

    @Override
    public void increaseStock(UUID id) {
        log.warn("Fallback: book-service not available.");
    }
}
