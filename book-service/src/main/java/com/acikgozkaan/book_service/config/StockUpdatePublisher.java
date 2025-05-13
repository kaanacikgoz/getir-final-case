package com.acikgozkaan.book_service.config;

import com.acikgozkaan.book_service.dto.BookStockEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class StockUpdatePublisher {
    private final Sinks.Many<BookStockEvent> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(BookStockEvent event) {
        sink.tryEmitNext(event);
    }

    public Flux<BookStockEvent> getStream() {
        return sink.asFlux();
    }
}