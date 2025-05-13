package com.acikgozkaan.borrowing_service.client;

import com.acikgozkaan.borrowing_service.client.fallback.BookClientFallback;
import com.acikgozkaan.borrowing_service.config.FeignClientInterceptor;
import com.acikgozkaan.borrowing_service.dto.external.BookResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.UUID;

@FeignClient(
        name = "book-service",
        path = "/api/v1/books",
        configuration = FeignClientInterceptor.class,
        fallback = BookClientFallback.class
)
public interface BookClient {

    @GetMapping("/{id}")
    BookResponse getBookById(@PathVariable("id") UUID id);

    @PutMapping("/{id}/decrease-stock")
    void decreaseStock(@PathVariable("id") UUID id);

    @PutMapping("/{id}/increase-stock")
    void increaseStock(@PathVariable("id") UUID id);

}