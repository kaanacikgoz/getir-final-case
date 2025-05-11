package com.acikgozkaan.borrowing_service.service;

import com.acikgozkaan.borrowing_service.dto.request.BorrowingRequest;
import com.acikgozkaan.borrowing_service.dto.response.BorrowingResponse;

import java.util.List;
import java.util.UUID;

public interface BorrowingService {

    BorrowingResponse borrowBook(BorrowingRequest request);

    BorrowingResponse returnBook(UUID borrowingId);

    List<BorrowingResponse> getAll();

    BorrowingResponse getById(UUID id);

    List<BorrowingResponse> getByUserId(UUID userId);

    List<BorrowingResponse> getOverdueBorrowings();

    String generateOverdueReport();
}