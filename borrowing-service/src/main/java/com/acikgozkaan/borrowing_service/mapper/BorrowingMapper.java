package com.acikgozkaan.borrowing_service.mapper;

import com.acikgozkaan.borrowing_service.dto.request.BorrowingRequest;
import com.acikgozkaan.borrowing_service.dto.response.BorrowingResponse;
import com.acikgozkaan.borrowing_service.entity.Borrowing;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BorrowingMapper {

    public Borrowing toEntity(BorrowingRequest request) {
        return Borrowing.builder()
                .userId(request.userId())
                .bookId(request.bookId())
                .borrowDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(14))
                .build();
    }

    public BorrowingResponse toResponse(Borrowing borrowing) {
        return new BorrowingResponse(
                borrowing.getId(),
                borrowing.getUserId(),
                borrowing.getBookId(),
                borrowing.getBorrowDate(),
                borrowing.getDueDate(),
                borrowing.getReturnDate()
        );
    }
}