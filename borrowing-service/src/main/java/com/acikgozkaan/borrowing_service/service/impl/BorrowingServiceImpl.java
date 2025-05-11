package com.acikgozkaan.borrowing_service.service.impl;

import com.acikgozkaan.borrowing_service.client.BookClient;
import com.acikgozkaan.borrowing_service.client.UserClient;
import com.acikgozkaan.borrowing_service.dto.external.BookResponse;
import com.acikgozkaan.borrowing_service.dto.request.BorrowingRequest;
import com.acikgozkaan.borrowing_service.dto.response.BorrowingResponse;
import com.acikgozkaan.borrowing_service.entity.Borrowing;
import com.acikgozkaan.borrowing_service.exception.BorrowingNotFoundException;
import com.acikgozkaan.borrowing_service.exception.BusinessRuleException;
import com.acikgozkaan.borrowing_service.exception.OutOfStockException;
import com.acikgozkaan.borrowing_service.mapper.BorrowingMapper;
import com.acikgozkaan.borrowing_service.repository.BorrowingRepository;
import com.acikgozkaan.borrowing_service.service.BorrowingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BorrowingMapper borrowingMapper;
    private final UserClient userClient;
    private final BookClient bookClient;

    @Override
    public BorrowingResponse borrowBook(BorrowingRequest request) {
        userClient.checkUserExists(request.userId());
        BookResponse book = bookClient.getBookById(request.bookId());

        try {
            bookClient.decreaseStock(request.bookId());
        } catch (FeignException.Conflict ex) {
            throw new OutOfStockException(book.id());
        }

        Borrowing borrowing = borrowingMapper.toEntity(request);
        Borrowing saved = borrowingRepository.save(borrowing);
        return borrowingMapper.toResponse(saved);
    }

    @Override
    public BorrowingResponse returnBook(UUID borrowingId) {
        Borrowing borrowing = getBorrowingOrThrow(borrowingId);

        if (borrowing.getReturnDate() != null) {
            throw new BusinessRuleException("Book is already returned");
        }

        borrowing.setReturnDate(LocalDate.now());
        Borrowing updated = borrowingRepository.save(borrowing);
        bookClient.increaseStock(borrowing.getBookId());
        return borrowingMapper.toResponse(updated);
    }

    @Override
    public List<BorrowingResponse> getAll() {
        return borrowingRepository.findAll()
                .stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    public BorrowingResponse getById(UUID id) {
        return borrowingMapper.toResponse(getBorrowingOrThrow(id));
    }

    @Override
    public List<BorrowingResponse> getByUserId(UUID userId) {
        return borrowingRepository.findByUserId(userId)
                .stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BorrowingResponse> getOverdueBorrowings() {
        return borrowingRepository.findByDueDateBeforeAndReturnDateIsNull(LocalDate.now())
                .stream()
                .map(borrowingMapper::toResponse)
                .toList();
    }

    @Override
    public String generateOverdueReport() {
        List<Borrowing> overdueList = borrowingRepository
                .findByDueDateBeforeAndReturnDateIsNull(LocalDate.now());

        long total = overdueList.size();
        Map<UUID, Long> userBorrowCounts = overdueList.stream()
                .collect(Collectors.groupingBy(Borrowing::getUserId, Collectors.counting()));

        StringBuilder report = new StringBuilder("""
                📚 OVERDUE BOOK REPORT
                -----------------------------
                Total Overdue Borrowings: %d
                Number of Unique Users: %d
                Last Generated: %s

                Per User Breakdown:
                """.formatted(total, userBorrowCounts.size(), LocalDate.now()));

        userBorrowCounts.forEach((userId, count) ->
                report.append("- User ID: ").append(userId)
                        .append(" → Overdue Books: ").append(count).append("\n")
        );

        return report.toString();
    }

    private Borrowing getBorrowingOrThrow(UUID id) {
        return borrowingRepository.findById(id)
                .orElseThrow(() -> new BorrowingNotFoundException(id));
    }

}